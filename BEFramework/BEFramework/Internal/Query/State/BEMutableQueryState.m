//
//  BEMutableQueryState.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEMutableQueryState.h"
#import "BEQueryState_Private.h"
#import "BEMacros.h"
#import "BEQueryConstants.h"

@interface BEMutableQueryState () {
  NSMutableDictionary<NSString *, id> *_conditions;
  NSMutableArray<NSString *> *_sortKeys;
  NSMutableSet<NSString *> *_includedKeys;
  NSMutableDictionary<NSString *, NSString *> *_extraOptions;
}

@end

@implementation BEMutableQueryState

@synthesize conditions = _conditions;
@synthesize sortKeys = _sortKeys;
@synthesize includedKeys = _includedKeys;
@synthesize extraOptions = _extraOptions;

@dynamic parseClassName;
@dynamic selectedKeys;
@dynamic limit;
@dynamic skip;
@dynamic cachePolicy;
@dynamic maxCacheAge;
@dynamic trace;
@dynamic shouldIgnoreACLs;
@dynamic shouldIncludeDeletingEventually;
@dynamic queriesLocalDatastore;
@dynamic localDatastorePinName;

///--------------------------------------
#pragma mark - Property Attributes
///--------------------------------------

+ (NSDictionary *)propertyAttributes {
  NSMutableDictionary *attributes = [[super propertyAttributes] mutableCopy];
  
  attributes[BEQueryStatePropertyName(conditions)] = [BEPropertyAttributes attributesWithAssociationType:BEPropertyInfoAssociationTypeMutableCopy];
  attributes[BEQueryStatePropertyName(sortKeys)] = [BEPropertyAttributes attributesWithAssociationType:BEPropertyInfoAssociationTypeMutableCopy];
  attributes[BEQueryStatePropertyName(includedKeys)] = [BEPropertyAttributes attributesWithAssociationType:BEPropertyInfoAssociationTypeMutableCopy];
  attributes[BEQueryStatePropertyName(extraOptions)] = [BEPropertyAttributes attributesWithAssociationType:BEPropertyInfoAssociationTypeMutableCopy];
  
  return attributes;
}

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithParseClassName:(NSString *)className {
  self = [self init];
  if (!self) return nil;
  
  _parseClassName = [className copy];
  
  return self;
}

+ (instancetype)stateWithParseClassName:(NSString *)className {
  return [[self alloc] initWithParseClassName:className];
}

///--------------------------------------
#pragma mark - Conditions
///--------------------------------------

- (void)setConditionType:(NSString *)type withObject:(id)object forKey:(NSString *)key {
  NSMutableDictionary *conditionObject = nil;
  
  // Check if we already have some sort of condition
  id existingCondition = _conditions[key];
  if ([existingCondition isKindOfClass:[NSMutableDictionary class]]) {
    conditionObject = existingCondition;
  }
  if (!conditionObject) {
    conditionObject = [NSMutableDictionary dictionary];
  }
  conditionObject[type] = object;
  
  [self setEqualityConditionWithObject:conditionObject forKey:key];
}

- (void)setEqualityConditionWithObject:(id)object forKey:(NSString *)key {
  if (!_conditions) {
    _conditions = [NSMutableDictionary dictionary];
  }
  _conditions[key] = object;
}

- (void)setRelationConditionWithObject:(id)object forKey:(NSString *)key {
  // We need to force saved BEObject here.
  NSMutableDictionary *condition = [NSMutableDictionary dictionaryWithCapacity:2];
  condition[@"object"] = object;
  condition[@"key"] = key;
  [self setEqualityConditionWithObject:condition forKey:BEQueryKeyRelatedTo];
}

- (void)removeAllConditions {
  [_conditions removeAllObjects];
}

///--------------------------------------
#pragma mark - Sort
///--------------------------------------

- (void)sortByKey:(NSString *)key ascending:(BOOL)ascending {
  [_sortKeys removeAllObjects];
  [self addSortKey:key ascending:ascending];
}

- (void)addSortKey:(NSString *)key ascending:(BOOL)ascending {
  if (!key) {
    return;
  }
  
  NSString *sortKey = (ascending ? key : [NSString stringWithFormat:@"-%@", key]);
  if (!_sortKeys) {
    _sortKeys = [NSMutableArray arrayWithObject:sortKey];
  } else {
    [_sortKeys addObject:sortKey];
  }
}

- (void)addSortKeysFromSortDescriptors:(NSArray<NSSortDescriptor *> *)sortDescriptors {
  [_sortKeys removeAllObjects];
  for (NSSortDescriptor *sortDescriptor in sortDescriptors) {
    [self addSortKey:sortDescriptor.key ascending:sortDescriptor.ascending];
  }
}

///--------------------------------------
#pragma mark - Includes
///--------------------------------------

- (void)includeKey:(NSString *)key {
  if (!_includedKeys) {
    _includedKeys = [NSMutableSet setWithObject:key];
  } else {
    [_includedKeys addObject:key];
  }
}

- (void)includeKeys:(NSArray<NSString *> *)keys {
  if (!_includedKeys) {
    _includedKeys = [NSMutableSet setWithArray:keys];
  } else {
    [_includedKeys addObjectsFromArray:keys];
  }
}

///--------------------------------------
#pragma mark - Selected Keys
///--------------------------------------

- (void)selectKeys:(NSArray<NSString *> *)keys {
  if (keys) {
    _selectedKeys = (_selectedKeys ? [_selectedKeys setByAddingObjectsFromArray:keys] : [NSSet setWithArray:keys]);
  } else {
    _selectedKeys = nil;
  }
}

///--------------------------------------
#pragma mark - Redirect
///--------------------------------------

- (void)redirectClassNameForKey:(NSString *)key {
  if (!_extraOptions) {
    _extraOptions = [NSMutableDictionary dictionary];
  }
  _extraOptions[@"redirectClassNameForKey"] = key;
}

@end
