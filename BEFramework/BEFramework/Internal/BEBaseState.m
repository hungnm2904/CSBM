//
//  BEBaseState.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEBaseState.h"

#import <objc/message.h>
#import <objc/runtime.h>

#import "BEAssert.h"
#import "BEHash.h"
#import "BEMacros.h"
#import "BEPropertyInfo.h"

@implementation BEPropertyAttributes

- (instancetype)init {
  return [self initWithAssociationType:BEPropertyInfoAssociationTypeDefault];
}
- (instancetype)initWithAssociationType:(BEPropertyInfoAssociationType)associationType {
  self = [super init];
  if (!self) return nil;
  
  _associationType = associationType;
  
  return self;
}

+ (instancetype)attributes {
  return [[self alloc] init];
}

+ (instancetype)attributesWithAssociationType:(BEPropertyInfoAssociationType)associationType {
  return [[self alloc] initWithAssociationType:associationType];
}

@end

@interface BEBaseState () {
  BOOL _initializing;
}

@end

@implementation BEBaseState

///--------------------------------------
#pragma mark - Property Info
///--------------------------------------

+ (NSSet *)_propertyInfo {
  static void *_propertyMapKey = &_propertyMapKey;
  static dispatch_queue_t queue;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    queue = dispatch_queue_create("com.parse.basestate.propertyinfo", DISPATCH_QUEUE_SERIAL);
  });
  
  __block NSMutableSet *results = nil;
  dispatch_sync(queue, ^{
    results = objc_getAssociatedObject(self, _propertyMapKey);
    if (results) {
      return;
    }
    
    NSDictionary *attributesMap = [(id<BEBaseStateSubclass>)self propertyAttributes];
    results = [[NSMutableSet alloc] initWithCapacity:attributesMap.count];
    
    [attributesMap enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
      [results addObject:[BEPropertyInfo propertyInfoWithClass:self
                                                          name:key
                                               associationType:[obj associationType]]];
    }];
    
    objc_setAssociatedObject(self, _propertyMapKey, results, OBJC_ASSOCIATION_RETAIN);
  });
  
  return results;
}

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init {
  // To prevent a recursive init function.
  if (_initializing) {
    return [super init];
  }
  
  _initializing = YES;
  return [self initWithState:nil];
}

- (instancetype)initWithState:(id)otherState {
  if (!_initializing) {
    _initializing = YES;
    
    self = [self init];
    if (!self) return nil;
  }
  
  NSSet *ourProperties = [[self class] _propertyInfo];
  NSSet *theirProperties = [[otherState class] _propertyInfo];
  
  NSMutableSet *shared = [ourProperties mutableCopy];
  [shared intersectSet:theirProperties];
  
  for (BEPropertyInfo *property in shared) {
    [property takeValueFrom:otherState toObject:self];
  }
  
  return self;
}

+ (instancetype)stateWithState:(BEBaseState *)otherState {
  return [[self alloc] initWithState:otherState];
}

///--------------------------------------
#pragma mark - Hashing
///--------------------------------------

- (NSUInteger)hash {
  NSUInteger result = 0;
  
  for (BEPropertyInfo *property in [[self class] _propertyInfo]) {
    result = BEIntegerPairHash(result, [[property getWrappedValueFrom:self] hash]);
  }
  
  return result;
}

///--------------------------------------
#pragma mark - Comparison
///--------------------------------------

- (NSComparisonResult)compare:(BEBaseState *)other {
  BEParameterAssert([other isKindOfClass:[BEBaseState class]],
                    @"Cannot compatre to an object that isn't a PFBaseState");
  
  NSSet *ourProperties = [[self class] _propertyInfo];
  NSSet *theirProperties = [[other class] _propertyInfo];
  
  NSMutableSet *shared = [ourProperties mutableCopy];
  [shared intersectSet:theirProperties];
  
  for (BEPropertyInfo *info in shared) {
    id ourValue = [info getWrappedValueFrom:self];
    id theirValue = [info getWrappedValueFrom:other];
    
    if (![ourValue respondsToSelector:@selector(compare:)]) {
      continue;
    }
    
    NSComparisonResult result = [ourValue compare:theirValue];
    if (result != NSOrderedSame) {
      return result;
    }
  }
  
  return NSOrderedSame;
}

///--------------------------------------
#pragma mark - Equality
///--------------------------------------

- (BOOL)isEqual:(id)other {
  if (self == other) {
    return YES;
  }
  
  if (![other isKindOfClass:[BEBaseState class]]) {
    return NO;
  }
  
  NSSet *ourProperties = [[self class] _propertyInfo];
  NSSet *theirProperties = [[other class] _propertyInfo];
  
  NSMutableSet *shared = [ourProperties mutableCopy];
  [shared intersectSet:theirProperties];
  
  for (BEPropertyInfo *info in shared) {
    id ourValue = [info getWrappedValueFrom:self];
    id theirValue = [info getWrappedValueFrom:other];
    
    if (ourValue != theirValue && ![ourValue isEqual:theirValue]) {
      return NO;
    }
  }
  
  return YES;
}

#pragma mark - Description
///--------------------------------------

// This allows us to easily use the same implementation for description and debugDescription
- (NSString *)descriptionWithValueSelector:(SEL)toPerform {
  NSMutableString *results = [NSMutableString stringWithFormat:@"<%@: %p", [self class], self];
  
  for (BEPropertyInfo *property in [[self class] _propertyInfo]) {
    id propertyValue = [property getWrappedValueFrom:self];
    NSString *propertyDescription = objc_msgSend_safe(NSString *)(propertyValue, toPerform);
    
    [results appendFormat:@", %@: %@", property.name, propertyDescription];
  }
  
  [results appendString:@">"];
  return results;
}

- (NSString *)description {
  return [self descriptionWithValueSelector:_cmd];
}

- (NSString *)debugDescription {
  return [self descriptionWithValueSelector:_cmd];
}

///--------------------------------------
#pragma mark - Dictionary/QuickLook representation
///--------------------------------------

- (id)nilValueForProperty:(NSString *)propertyName {
  return [NSNull null];
}

// Implementation detail - this returns a mutable dictionary with mutable leaves.
- (NSDictionary *)dictionaryRepresentation {
  NSSet *properties = [[self class] _propertyInfo];
  NSMutableDictionary *results = [[NSMutableDictionary alloc] initWithCapacity:properties.count];
  
  for (BEPropertyInfo *info in properties) {
    id value = [info getWrappedValueFrom:self];
    
    if (value == nil) {
      value = [self nilValueForProperty:info.name];
      
      if (value == nil) {
        continue;
      }
    }
    
    results[info.name] = value;
  }
  
  return results;
}

- (id)debugQuickLookObject {
  return [self dictionaryRepresentation].description;
}
@end
