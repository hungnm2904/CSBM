//
//  BEObjectState.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEObjectState.h"
#import "BEObjectState_Private.h"
#import "BEDateFormatter.h"
#import "BEEncoder.h"
#import "BEMutableUserState.h"
#import "BEObjectConstant.h"
#import "BEObjectUtilities.h"
#import "BEMutableObjectState.h"

@implementation BEObjectState

- (instancetype)init {
  self = [super init];
  if (!self) return nil;
  
  _serverData = [NSMutableDictionary dictionary];
  
  return [super init];
}

- (instancetype)initWithState:(BEObjectState *)state {
  self = [super init];
  if (!self) return nil;
  
  _csbmClassName = [state.csbmClassName copy];
  _objectId = [state.objectId copy];
  
  _updatedAt = state.updatedAt;
  _createdAt = state.createdAt;
  
  _serverData = [state.serverData mutableCopy] ?: [NSMutableDictionary dictionary];
  
  _complete = state.complete;
  _deleted = state.deleted;
  
  return self;
}

- (instancetype)initWithCsbmClassName:(NSString *)csbmClassName {
  return [self initWithCsbmClassName:csbmClassName objectId: nil isComplete:NO];
}

- (instancetype)initWithCsbmClassName:(NSString *)csbmClassName objectId:(NSString *)objectId isComplete:(BOOL)complete {
  self = [self init];
  if (!self) return nil;
  
  _csbmClassName = [csbmClassName copy];
  _objectId = [objectId copy];
  _complete = complete;
  
  return self;
}

- (instancetype)initWithState:(BEObjectState *)state mutatingBlock:(BEObjectStateMutationBlock)block {
  self = [self initWithState:state];
  if (!self) return nil;
  
  block((BEMutableObjectState *)self);
  
  return self;
}

+ (instancetype)stateWithState:(BEObjectState *)state {
  return [[self alloc] initWithState:state];
}

+ (instancetype)stateWithParseClassName:(NSString *)parseClassName {
  return [[self alloc] initWithCsbmClassName:parseClassName];
}

+ (instancetype)stateWithParseClassName:(NSString *)parseClassName
                               objectId:(NSString *)objectId
                             isComplete:(BOOL)complete {
  return [[self alloc] initWithCsbmClassName:parseClassName
                                     objectId:objectId
                                   isComplete:complete];
}

///--------------------------------------
#pragma mark - Accessors
///--------------------------------------s

- (void)setServerData:(NSDictionary *)serverData {
  if (self.serverData != serverData) {
    _serverData = [serverData mutableCopy];
  }
}

///--------------------------------------
#pragma mark - Coding
///--------------------------------------

- (NSDictionary *)dictionaryRepresentationWithObjectEncoder:(BEEncoder *)objectEncoder {
  NSMutableDictionary *result = [NSMutableDictionary dictionary];
  if (self.objectId) {
    result[BEObjectObjectIdRESTKey] = self.objectId;
  }
  if (self.createdAt) {
    result[BEObjectCreatedAtRESTKey] = [[BEDateFormatter sharedFormatter] preciseStringFromDate:self.createdAt];
  }
  if (self.updatedAt) {
    result[BEObjectUpdatedAtRESTKey] = [[BEDateFormatter sharedFormatter] preciseStringFromDate:self.updatedAt];
  }
  [self.serverData enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
    result[key] = [objectEncoder encodeObject:obj];
  }];
  return [result copy];
}
///--------------------------------------
#pragma mark - PFObjectState (Mutable)
///--------------------------------------

#pragma mark Accessors

- (void)setServerDataObject:(id)object forKey:(NSString *)key {
  _serverData[key] = object;
}

- (void)removeServerDataObjectForKey:(NSString *)key {
  [_serverData removeObjectForKey:key];
}

- (void)removeServerDataObjectsForKeys:(NSArray *)keys {
  [_serverData removeObjectsForKeys:keys];
}

- (void)setCreatedAtFromString:(NSString *)string {
  self.createdAt = [[BEDateFormatter sharedFormatter] dateFromString:string];
}

- (void)setUpdatedAtFromString:(NSString *)string {
  self.updatedAt = [[BEDateFormatter sharedFormatter] dateFromString:string];
}

#pragma mark Apply

- (void)applyState:(BEObjectState *)state {
  if (state.objectId) {
    self.objectId = state.objectId;
  }
  if (state.createdAt) {
    self.createdAt = state.createdAt;
  }
  if (state.updatedAt) {
    self.updatedAt = state.updatedAt;
  }
  [_serverData addEntriesFromDictionary:state.serverData];
  
  self.complete |= state.complete;
}

- (void)applyOperationSet:(PFOperationSet *)operationSet {
  [BEObjectUtilities applyOperationSet:operationSet toDictionary:_serverData];
}

///--------------------------------------
#pragma mark - Mutating
///--------------------------------------

- (BEObjectState *)copyByMutatingWithBlock:(BEObjectStateMutationBlock)block {
  return [[BEObjectState alloc] initWithState:self mutatingBlock:block];
}

///--------------------------------------
#pragma mark - NSCopying
///--------------------------------------

- (id)copyWithZone:(NSZone *)zone {
  return [[BEObjectState allocWithZone:zone] initWithState:self];
}

///--------------------------------------
#pragma mark - NSMutableCopying
///--------------------------------------

- (id)mutableCopyWithZone:(NSZone *)zone {
  return [[BEMutableObjectState allocWithZone:zone] initWithState:self];
}
@end
