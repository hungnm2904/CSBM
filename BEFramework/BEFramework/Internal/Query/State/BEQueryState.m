//
//  BEQueryState.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEQueryState.h"
#import "BEQueryState_Private.h"

#import "BEMutableQueryState.h"
#import "BEPropertyInfo.h"
#import "BEMacros.h"

@implementation BEQueryState

///--------------------------------------
#pragma mark - BEBaseStateSubclass
///--------------------------------------

+ (NSDictionary *)propertyAttributes {
  return @{
           BEQueryStatePropertyName(parseClassName): [BEPropertyAttributes attributesWithAssociationType:BEPropertyInfoAssociationTypeCopy],
           BEQueryStatePropertyName(conditions): [BEPropertyAttributes attributesWithAssociationType:BEPropertyInfoAssociationTypeCopy],
           BEQueryStatePropertyName(sortKeys): [BEPropertyAttributes attributesWithAssociationType:BEPropertyInfoAssociationTypeCopy],
           BEQueryStatePropertyName(includedKeys): [BEPropertyAttributes attributesWithAssociationType:BEPropertyInfoAssociationTypeCopy],
           BEQueryStatePropertyName(selectedKeys): [BEPropertyAttributes attributesWithAssociationType:BEPropertyInfoAssociationTypeCopy],
           BEQueryStatePropertyName(extraOptions): [BEPropertyAttributes attributesWithAssociationType:BEPropertyInfoAssociationTypeCopy],
           
           BEQueryStatePropertyName(limit): [BEPropertyAttributes attributes],
           BEQueryStatePropertyName(skip): [BEPropertyAttributes attributes],
           BEQueryStatePropertyName(cachePolicy): [BEPropertyAttributes attributes],
           BEQueryStatePropertyName(maxCacheAge): [BEPropertyAttributes attributes],
           
           BEQueryStatePropertyName(trace): [BEPropertyAttributes attributes],
           BEQueryStatePropertyName(shouldIgnoreACLs): [BEPropertyAttributes attributes],
           BEQueryStatePropertyName(shouldIncludeDeletingEventually): [BEPropertyAttributes attributes],
           BEQueryStatePropertyName(queriesLocalDatastore): [BEPropertyAttributes attributes],
           
           BEQueryStatePropertyName(localDatastorePinName): [BEPropertyAttributes attributesWithAssociationType:BEPropertyInfoAssociationTypeCopy]
           };
}

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init {
  self = [super init];
  if (!self) return nil;
  
  _cachePolicy = kBECachePolicyIgnoreCache;
  _maxCacheAge = INFINITY;
  _limit = -1;
  
  return self;
}

- (instancetype)initWithState:(BEQueryState *)state {
  return [super initWithState:state];
}

+ (instancetype)stateWithState:(BEQueryState *)state {
  return [super stateWithState:state];
}

///--------------------------------------
#pragma mark - Accessors
///--------------------------------------

- (NSString *)sortOrderString {
  return [self.sortKeys componentsJoinedByString:@","];
}

///--------------------------------------
#pragma mark - Mutable Copying
///--------------------------------------

- (id)copyWithZone:(NSZone *)zone {
  return [[BEQueryState allocWithZone:zone] initWithState:self];
}

//- (instancetype)mutableCopyWithZone:(NSZone *)zone {
//  return [[BEMutableQueryState allocWithZone:zone] initWithState:self];
//}

@end
