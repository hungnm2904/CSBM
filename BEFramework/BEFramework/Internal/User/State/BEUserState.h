//
//  BEUserState.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEObjectState.h"

@class BEMutableUserState;

typedef void(^BEUserStateMutationBlock)(BEMutableUserState *state);

@interface BEUserState : BEObjectState

@property (nonatomic, copy, readonly) NSString *sessionToken;
@property (nonatomic, copy, readonly) NSDictionary *authData;

@property (nonatomic, assign, readonly) BOOL isNew;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithState:(BEUserState *)state;
- (instancetype)initWithState:(BEUserState *)state mutatingBlock:(BEUserStateMutationBlock)block;
+ (instancetype)stateWithState:(BEUserState *)state;

///--------------------------------------
#pragma mark - Mutating
///--------------------------------------

- (BEUserState *)copyByMutatingWithBlock:(BEUserStateMutationBlock)block;

@end
