//
//  BEObjectSubclassingController.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEObjectSubclassingController.h"

#import <objc/runtime.h>

#import "BEObject.h"
#import "BESubclassing.h"
#import "BEAssert.h"
#import "BEMacros.h"
#import "BEObjectSubclassInfo.h"
#import "BEPropeprtyInfo_Private.h"
#import "BEPropertyInfo_Runtime.h"

// CFNumber does not use number type 0, we take advantage of that here.
#define kCFNumberTypeUnknown 0

static CFNumberType BENumberTypeForObjCType(const char *encodedType) {
  // To save anyone in the future from some major headaches, sanity check here.
#if kCFNumberTypeMax > UINT8_MAX
#error kCFNumberTypeMax has been changed! This solution will no longer work.
#endif
  
  // Organizing the table this way makes it nicely fit into two cache lines. This makes lookups nearly free, even more
  // so if repeated.
  static uint8_t types[128] = {
    // Core types.
    ['c'] = kCFNumberCharType,
    ['i'] = kCFNumberIntType,
    ['s'] = kCFNumberShortType,
    ['l'] = kCFNumberLongType,
    ['q'] = kCFNumberLongLongType,
    
    // CFNumber (and NSNumber, actually) does not store unsigned types.
    // This may cause some strange issues when dealing with values near the max for that type.
    // We should investigate this if it becomes a problem.
    ['C'] = kCFNumberCharType,
    ['I'] = kCFNumberIntType,
    ['S'] = kCFNumberShortType,
    ['L'] = kCFNumberLongType,
    ['Q'] = kCFNumberLongLongType,
    
    // Floating point
    ['f'] = kCFNumberFloatType,
    ['d'] = kCFNumberDoubleType,
    
    // C99 & CXX boolean. We are keeping this here for decoding, as you can safely use CFNumberGetBytes on a
    // CFBoolean, and extract it into a char.
    ['B'] = kCFNumberCharType,
  };
  
  return (CFNumberType)types[encodedType[0]];
}

static NSNumber *BENumberCreateSafe(const char *typeEncoding, const void *bytes) {
  // NOTE: This is required because NSJSONSerialization treats all NSNumbers with the 'char' type as numbers, not
  // booleans. As such, we must treat any and all boolean type encodings as explicit booleans, otherwise we will
  // send '1' and '0' to the api server rather than 'true' and 'false'.
  //
  // TODO (richardross): When we drop support for 10.9/iOS 7, remove the 'c' encoding and only use the new 'B'
  // encoding.
  if (typeEncoding[0] == 'B' || typeEncoding[0] == 'c') {
    return [NSNumber numberWithBool:*(BOOL *)bytes];
  }
  
  CFNumberType numberType = BENumberTypeForObjCType(typeEncoding);
  BEConsistencyAssert(numberType != kCFNumberTypeUnknown, @"Unsupported type encoding %s!", typeEncoding);
  return (__bridge_transfer NSNumber *)CFNumberCreate(NULL, numberType, bytes);
}

@implementation BEObjectSubclassingController {
  dispatch_queue_t _registeredSubclassesAccessQueue;
  NSMutableDictionary *_registeredSubclasses;
  NSMutableDictionary *_unregisteredSubclasses;
}

static BEObjectSubclassingController *defaultController_;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init {
  self = [super init];
  if (!self) return nil;
  
  _registeredSubclassesAccessQueue = dispatch_queue_create("com.parse.object.subclassing", DISPATCH_QUEUE_SERIAL);
  _registeredSubclasses = [NSMutableDictionary dictionary];
  _unregisteredSubclasses = [NSMutableDictionary dictionary];
  
  return self;
}

+ (instancetype)defaultController {
  if (!defaultController_) {
    defaultController_ = [[BEObjectSubclassingController alloc] init];
  }
  return defaultController_;
}

+ (void)clearDefaultController {
  defaultController_ = nil;
}

///--------------------------------------
#pragma mark - Public
///--------------------------------------

- (Class<BESubclassing>)subclassForcsbmClassName:(NSString *)csbmClassName {
  __block Class result = nil;
  BE_sync_with_throw(_registeredSubclassesAccessQueue, ^{
    result = [_registeredSubclasses[csbmClassName] subclass];
  });
  return result;
}

- (void)registerSubclass:(Class<BESubclassing>)kls {
  BE_sync_with_throw(_registeredSubclassesAccessQueue, ^{
    [self _rawRegisterSubclass:kls];
  });
}

- (void)unregisterSubclass:(Class<BESubclassing>)class {
  BE_sync_with_throw(_registeredSubclassesAccessQueue, ^{
    NSString *csbmClassName = [class csbmClassName];
    Class registeredClass = [_registeredSubclasses[csbmClassName] subclass];
    
    // Make it a no-op if the class itself is not registered or
    // if there is another class registered under the same name.
    if (registeredClass == nil ||
        ![registeredClass isEqual:class]) {
      return;
    }
    
    [_registeredSubclasses removeObjectForKey:csbmClassName];
  });
}

- (BOOL)forwardObjectInvocation:(NSInvocation *)invocation withObject:(BEObject<BESubclassing> *)object {
  BEObjectSubclassInfo *subclassInfo = [self _subclassInfoForClass:[object class]];
  
  BOOL isSetter = NO;
  BEPropertyInfo *propertyInfo = [subclassInfo propertyInfoForSelector:invocation.selector isSetter:&isSetter];
  if (!propertyInfo) {
    return NO;
  }
  
  if (isSetter) {
    [self _forwardSetterInvocation:invocation forProperty:propertyInfo withObject:object];
  } else {
    [self _forwardGetterInvocation:invocation forProperty:propertyInfo withObject:object];
  }
  return YES;
}

- (NSMethodSignature *)forwardingMethodSignatureForSelector:(SEL)cmd ofClass:(Class<BESubclassing>)kls {
  BEObjectSubclassInfo *subclassInfo = [self _subclassInfoForClass:kls];
  return [subclassInfo forwardingMethodSignatureForSelector:cmd];
}

///--------------------------------------
#pragma mark - Private
///--------------------------------------

- (void)_forwardGetterInvocation:(NSInvocation *)invocation
                     forProperty:(BEPropertyInfo *)propertyInfo
                      withObject:(BEObject *)object {
  BEConsistencyAssert(invocation.methodSignature.numberOfArguments == 2, @"Getter should take no arguments!");
  BEConsistencyAssert(invocation.methodSignature.methodReturnType[0] != 'v', @"A getter cannot return void!");
  
  const char *methodReturnType = invocation.methodSignature.methodReturnType;
  void *returnValueBytes = alloca(invocation.methodSignature.methodReturnLength);
  
  if (propertyInfo.ivar) {
    object_getIvarValue_safe(object, propertyInfo.ivar, returnValueBytes, propertyInfo.associationType);
  } else {
    __autoreleasing id dictionaryValue = nil;
    if ([propertyInfo.typeEncoding isEqualToString:@"@\"BERelation\""]) {
      dictionaryValue = [object relationForKey:propertyInfo.name];
    } else {
      dictionaryValue = object[propertyInfo.name];
      
      // TODO: (richardross) Investigate why we were orignally copying the result of -objectForKey,
      // as this doens't seem right.
      if (propertyInfo.associationType == BEPropertyInfoAssociationTypeCopy) {
        dictionaryValue = [dictionaryValue copy];
      }
    }
    
    if (dictionaryValue == nil || [dictionaryValue isKindOfClass:[NSNull class]]) {
      memset(returnValueBytes, 0, invocation.methodSignature.methodReturnLength);
    } else if (methodReturnType[0] == '@') {
      memcpy(returnValueBytes, (void *) &dictionaryValue, sizeof(id));
    } else if ([dictionaryValue isKindOfClass:[NSNumber class]]) {
      CFNumberGetValue((__bridge CFNumberRef) dictionaryValue,
                       BENumberTypeForObjCType(methodReturnType),
                       returnValueBytes);
    } else {
      // TODO:(richardross)Support C-style structs that automatically convert to JSON via NSValue?
      BEConsistencyAssert(false, @"Unsupported type encoding %s!", methodReturnType);
    }
  }
  
  [invocation setReturnValue:returnValueBytes];
}

- (void)_forwardSetterInvocation:(NSInvocation *)invocation
                     forProperty:(BEPropertyInfo *)propertyInfo
                      withObject:(BEObject *)object {
  BEConsistencyAssert(invocation.methodSignature.numberOfArguments == 3, @"Setter should only take 1 argument!");
  
  BEObject *sourceObject = object;
  const char *argumentType = [invocation.methodSignature getArgumentTypeAtIndex:2];
  
  NSUInteger argumentValueSize = 0;
  NSGetSizeAndAlignment(argumentType, &argumentValueSize, NULL);
  
  void *argumentValueBytes = alloca(argumentValueSize);
  [invocation getArgument:argumentValueBytes atIndex:2];
  
  if (propertyInfo.ivar) {
    object_setIvarValue_safe(sourceObject, propertyInfo.ivar, argumentValueBytes, propertyInfo.associationType);
  } else {
    id dictionaryValue = nil;
    
    if (argumentType[0] == '@') {
      dictionaryValue = *(__unsafe_unretained id *)argumentValueBytes;
      
      if (propertyInfo.associationType == BEPropertyInfoAssociationTypeCopy) {
        dictionaryValue = [dictionaryValue copy];
      }
    } else {
      dictionaryValue = BENumberCreateSafe(argumentType, argumentValueBytes);
    }
    
    if (dictionaryValue == nil) {
      [sourceObject removeObjectForKey:propertyInfo.name];
    } else {
      sourceObject[propertyInfo.name] = dictionaryValue;
    }
  }
}

- (BEObjectSubclassInfo *)_subclassInfoForClass:(Class<BESubclassing>)kls {
  __block BEObjectSubclassInfo *result = nil;
  BE_sync_with_throw(_registeredSubclassesAccessQueue, ^{
    if (class_respondsToSelector(object_getClass(kls), @selector(csbmClassName))) {
      result = _registeredSubclasses[[kls csbmClassName]];
    }
    
    // TODO: (nlutsenko, richardross) Don't let unregistered subclasses have dynamic property resolution.
    if (!result) {
      result = [BEObjectSubclassInfo subclassInfoWithSubclass:kls];
      _unregisteredSubclasses[NSStringFromClass(kls)] = result;
    }
  });
  return result;
}

// Reverse compatibility note: many people may have built BEObject subclasses before
// we officially supported them. Our implementation can do cool stuff, but requires
// the csbmClassName class method.
- (void)_rawRegisterSubclass:(Class)kls {
  BEConsistencyAssert([kls conformsToProtocol:@protocol(BESubclassing)],
                      @"Can only call +registerSubclass on subclasses conforming to BESubclassing.");
  
  NSString *csbmClassName = [kls csbmClassName];
  
  // Bug detection: don't allow subclasses of subclasses (i.e. custom user classes)
  // to change the value of +csbmClassName
  if ([kls superclass] != [BEObject class]) {
    // We compare Method definitions against the BEObject version witout invoking it
    // because that Method could throw on an intermediary class which is
    // not meant for direct use.
    Method baseImpl = class_getClassMethod([BEObject class], @selector(csbmClassName));
    Method superImpl = class_getClassMethod([kls superclass], @selector(csbmClassName));
    
    BEConsistencyAssert(superImpl == baseImpl ||
                        [csbmClassName isEqualToString:[[kls superclass] csbmClassName]],
                        @"Subclasses of subclasses may not have separate +csbmClassName "
                        "definitions. %@ should inherit +csbmClassName from %@.",
                        kls, [kls superclass]);
  }
  
  Class current = [_registeredSubclasses[csbmClassName] subclass];
  if (current && current != kls) {
    // We've already registered a more specific subclass (i.e. we're calling
    // registerSubclass:BEUser after MYUser
    if ([current isSubclassOfClass:kls]) {
      return;
    }
    
    BEConsistencyAssert([kls isSubclassOfClass:current],
                        @"Tried to register both %@ and %@ as the native BEObject subclass "
                        "of %@. Cannot determine the right class to use because neither "
                        "inherits from the other.", current, kls, csbmClassName);
  }
  
  // Move the subclass info from unregisteredSubclasses dictionary to registered ones, or create if it doesn't exist.
  NSString *className = NSStringFromClass(kls);
  BEObjectSubclassInfo *subclassInfo = _unregisteredSubclasses[className];
  if (subclassInfo) {
    [_unregisteredSubclasses removeObjectForKey:className];
  } else {
    subclassInfo = [BEObjectSubclassInfo subclassInfoWithSubclass:kls];
  }
  _registeredSubclasses[[kls csbmClassName]] = subclassInfo;
}

@end

