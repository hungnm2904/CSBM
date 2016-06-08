//
//  BEUserState.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEUserState.h"
#import "BEObjectState_Private.h"

#import "BEMutableUserState.h"
#import "BEUserState_Private.h"
#import "BEUserConstants.h"

@implementation BEUserState

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithState:(BEUserState *)state {
  self = [super initWithState:state];
  if (!self) return nil;
  
  _sessionToken = [state.sessionToken copy];
  _authData = [state.authData copy];
  _isNew = state.isNew;
  
  return self;
}

- (instancetype)initWithState:(BEUserState *)state mutatingBlock:(BEUserStateMutationBlock)block {
  self = [self initWithState:state];
  if (!self) return nil;
  
  block((BEMutableUserState *)self);
  
  return self;
}

+ (instancetype)stateWithState:(BEUserState *)state {
  return [super stateWithState:state];
}

///--------------------------------------
#pragma mark - Serialization
///--------------------------------------

- (NSDictionary *)dictionaryRepresentationWithObjectEncoder:(BEEncoder *)objectEncoder {
  NSMutableDictionary *dictionary = [[super dictionaryRepresentationWithObjectEncoder:objectEncoder] mutableCopy];
  [dictionary removeObjectForKey:BEUserPasswordRESTKey];
  return dictionary;
}

///--------------------------------------
#pragma mark - Mutating
///--------------------------------------

- (BEUserState *)copyByMutatingWithBlock:(BEUserStateMutationBlock)block {
  return [[BEUserState alloc] initWithState:self mutatingBlock:block];
}

///--------------------------------------
#pragma mark - NSCopying
///--------------------------------------

- (id)copyWithZone:(NSZone *)zone {
  return [[BEUserState allocWithZone:zone] initWithState:self];
}

///--------------------------------------
#pragma mark - NSMutableCopying
///--------------------------------------

- (id)mutableCopyWithZone:(NSZone *)zone {
  return [[BEMutableUserState allocWithZone:zone] initWithState:self];
}

@end
