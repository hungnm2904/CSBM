//
//  BEObjectState.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
@class BEEncoder;
@class BEMutableObjectState;

typedef void(^BEObjectStateMutationBlock)(BEMutableObjectState *state);
@interface BEObjectState : NSObject <NSCopying, NSMutableCopying>

@property (nonatomic, copy, readonly) NSString *csbmClassName;
@property (nonatomic, copy, readonly) NSString *objectId;

@property (nonatomic, strong, readonly) NSDate *createdAt;
@property (nonatomic, strong, readonly) NSDate *updatedAt;

@property (nonatomic, copy, readonly) NSDictionary *serverData;

@property (nonatomic, assign, readonly, getter=isComplete) BOOL complete;
@property (nonatomic, assign, readonly, getter=isDeleted) BOOL deleted;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init NS_DESIGNATED_INITIALIZER;

- (instancetype)initWithState:(BEObjectState *)state NS_REQUIRES_SUPER;
- (instancetype)initWithState:(BEObjectState *)state mutatingBlock:(BEObjectStateMutationBlock)block;
- (instancetype)initWithCsbmClassName:(NSString *)csbmClassName;
- (instancetype)initWithCsbmClassName:(NSString *)csbmClassName
                              objectId:(NSString *)objectId
                            isComplete:(BOOL)complete;

+ (instancetype)stateWithState:(BEObjectState *)state NS_REQUIRES_SUPER;
+ (instancetype)stateWithCsbmClassName:(NSString *)csbmClassName;
+ (instancetype)stateWithCsbmClassName:(NSString *)csbmClassName
                               objectId:(NSString *)objectId
                             isComplete:(BOOL)complete;

///--------------------------------------
#pragma mark - Coding
///--------------------------------------

/**
 Encodes all fields in `serverData`, `objectId`, `createdAt` and `updatedAt` into objects suitable for JSON/Persistence.
 
 @note `csbmClassName` isn't automatically added to the dictionary.
 
 @param objectEncoder Encoder to use to encode custom objects.
 
 @return `NSDictionary` instance representing object state.
 */
- (NSDictionary *)dictionaryRepresentationWithObjectEncoder:(BEEncoder *)objectEncoder NS_REQUIRES_SUPER;

///--------------------------------------
#pragma mark - Mutating
///--------------------------------------

- (BEObjectState *)copyByMutatingWithBlock:(BEObjectStateMutationBlock)block;
@end
