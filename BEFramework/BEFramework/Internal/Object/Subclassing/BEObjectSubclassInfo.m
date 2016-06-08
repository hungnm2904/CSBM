//
//  BEObjectSubclassInfo.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEObjectSubclassInfo.h"
#import <objc/runtime.h>
#import "BEAssert.h"
#import "BELogging.h"
#import "BEMacros.h"

#import "BEPropeprtyInfo_Private.h"

///--------------------------------------
#pragma mark - Helper
///--------------------------------------

static BOOL startsWith(const char *string, const char *prefix) {
  // Keep iterating in lockstep. If we run out of prefix letters first,
  // this is a valid prefix.
  for (; *string && *prefix && *prefix == *string; ++string, ++prefix)
  ;
  return !*prefix;
}

// This method helps us get our bearings regardless of whether we were passed
// setFoo: or foo. We'll always exit this method by setting outPair to
// [accessor, mutator] and returns the property they correspond to. If the
// property cannot be found, returns NULL and outPair is undefined.
// An objc_property_t is an opaque struct pointer containing a SEL name and char *
// type information which follows a DSL explained in the Objective-C Runtime Reference.
static objc_property_t getAccessorMutatorPair(Class klass, SEL sel, SEL outPair[2]) {
  const char *selName = sel_getName(sel);
  ptrdiff_t selNameByteLen = strlen(selName) + 1;
  char temp[selNameByteLen + 4];
  
  if (startsWith(selName, "set")) {
    outPair[1] = sel;
    memcpy(temp, selName + 3, selNameByteLen - 3);
    temp[0] -= 'A' - 'a';
    
    temp[selNameByteLen - 5] = 0; // drop ':'
    outPair[0] = sel_registerName(temp);
  } else {
    outPair[0] = sel;
    sprintf(temp, "set%s:", selName);
    if (selName[0] >= 'a' && selName[0] <= 'z') {
      temp[3] += 'A' - 'a';
    }
    outPair[1] = sel_registerName(temp);
  }
  
  const char *propName = sel_getName(outPair[0]);
  objc_property_t property = class_getProperty(klass, propName);
  if (!property) {
    // The user could have broken convention and declared an upper case property.
    memcpy(temp, propName, strlen(propName) + 1);
    temp[0] += 'A' - 'a';
    outPair[0] = sel_registerName(temp);
    property = class_getProperty(klass, temp);
  }
  return property;
}

@implementation BEObjectSubclassInfo {
  dispatch_queue_t _dataAccessQueue;
  NSMutableDictionary *_knownProperties;
  NSMutableDictionary *_knownMethodSignatures;
}

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithSubclass:(Class)kls {
  self = [super init];
  if (!self) return nil;
  
  _dataAccessQueue = dispatch_queue_create("com.parse.object.subclassing.data.access", DISPATCH_QUEUE_SERIAL);
  _subclass = kls;
  
  _knownProperties = [NSMutableDictionary dictionary];
  _knownMethodSignatures = [NSMutableDictionary dictionary];
  
  return self;
}

+ (instancetype)subclassInfoWithSubclass:(Class)kls {
  return [[self alloc] initWithSubclass:kls];
}

///--------------------------------------
#pragma mark - Public
///--------------------------------------

- (BEPropertyInfo *)propertyInfoForSelector:(SEL)cmd isSetter:(BOOL *)isSetter {
  __block BEPropertyInfo *result = nil;
  dispatch_sync(_dataAccessQueue, ^{
    result = [self _rawPropertyInfoForSelector:cmd];
  });
  
  if (isSetter) {
    *isSetter = (cmd == result.setterSelector);
  }
  
  return result;
}

- (NSMethodSignature *)forwardingMethodSignatureForSelector:(SEL)cmd {
  __block NSMethodSignature *result = nil;
  NSString *selectorString = NSStringFromSelector(cmd);
  
  // NSMethodSignature can be fairly heavyweight, so let's agressively cache this here.
  dispatch_sync(_dataAccessQueue, ^{
    result = _knownMethodSignatures[selectorString];
    if (result) {
      return;
    }
    
    BEPropertyInfo *propertyInfo = [self _rawPropertyInfoForSelector:cmd];
    if (!propertyInfo) {
      return;
    }
    
    BOOL isSetter = (cmd == propertyInfo.setterSelector);
    NSString *typeEncoding = propertyInfo.typeEncoding;
    
    // Property type encoding includes the class name as well.
    // This is fine, except for the fact that NSMethodSignature hates that.
    NSUInteger startLocation = [typeEncoding rangeOfString:@"\"" options:0].location;
    NSUInteger endLocation = [typeEncoding rangeOfString:@"\""
                                                 options:NSBackwardsSearch | NSAnchoredSearch].location;
    
    if (startLocation != NSNotFound && endLocation != NSNotFound) {
      typeEncoding = [typeEncoding substringToIndex:startLocation];
    }
    
    NSString *objcTypes = ([NSString stringWithFormat:(isSetter ? @"v@:%@" : @"%@@:"), typeEncoding]);
    result = [NSMethodSignature signatureWithObjCTypes:objcTypes.UTF8String];
    
    _knownMethodSignatures[selectorString] = result;
  });
  
  return result;
}

///--------------------------------------
#pragma mark - Private
///--------------------------------------

- (BEPropertyInfo *)_rawPropertyInfoForSelector:(SEL)cmd {
  BEPropertyInfo *result = nil;
  NSString *selectorString = NSStringFromSelector(cmd);
  result = _knownProperties[selectorString];
  if (result) {
    return result;
  }
  
  SEL propertySelectors[2];
  objc_property_t property = getAccessorMutatorPair(self.subclass, cmd, propertySelectors);
  if (!property) {
    return nil;
  }
  
  // Check if we've registered this property with a different name.
  NSString *propertyName = @(property_getName(property));
  result = _knownProperties[propertyName];
  if (result) {
    // Re-register it with the name we just searched for for faster future lookup.
    _knownProperties[selectorString] = result;
    return result;
  }
  
  const char *attributes = property_getAttributes(property);
  if (strstr(attributes, "T@\"BERelation\",") == attributes && !strstr(attributes, ",R")) {
    BELogWarning(BELoggingTagCommon,
                 @"BERelation properties are always readonly, but %@.%@ was declared otherwise.",
                 self.subclass, selectorString);
  }
  
  result = [BEPropertyInfo propertyInfoWithClass:self.subclass name:propertyName];
  
  _knownProperties[result.name] = result;
  if (result.getterSelector) {
    _knownProperties[NSStringFromSelector(result.getterSelector)] = result;
  }
  if (result.setterSelector) {
    _knownProperties[NSStringFromSelector(result.setterSelector)] = result;
  }
  
  return result;
}

@end
