//
//  BEConfig.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <Bolts/BFTask.h>
#import "BEConstants.h"

@class BEConfig;

typedef void(^BEConfigResultBlock)(BEConfig *_Nullable config, NSError *_Nullable error);

/**
 `BEConfig` is a representation of the remote configuration object.
 It enables you to add things like feature gating, a/b testing or simple "Message of the day".
 */
@interface BEConfig : NSObject

///--------------------------------------
#pragma mark - Current Config
///--------------------------------------

/**
 Returns the most recently fetched config.
 
 If there was no config fetched - this method will return an empty instance of `BEConfig`.
 
 @return Current, last fetched instance of BEConfig.
 */
+ (BEConfig *)currentConfig;

/**
 Returns the task that encapsulates the most recently fetched config.
 
 If there was no config fetched - this method will return an empty instance of `BEConfig`.
 
 @return Task that encapsulates current, last fetched instance of BEConfig.
 */
+ (BFTask<BEConfig *> *)getCurrentConfigInBackground;

///--------------------------------------
#pragma mark - Retrieving Config
///--------------------------------------

/**
 Gets the `BEConfig` *asynchronously* and sets it as a result of a task.
 
 @return The task, that encapsulates the work being done.
 */
+ (BFTask<BEConfig *> *)getConfigInBackground;

/**
 Gets the `BEConfig` *asynchronously* and executes the given callback block.
 
 @param block The block to execute.
 It should have the following argument signature: `^(BEConfig *config, NSError *error)`.
 */
+ (void)getConfigInBackgroundWithBlock:(nullable BEConfigResultBlock)block;

///--------------------------------------
#pragma mark - Parameters
///--------------------------------------

/**
 Returns the object associated with a given key.
 
 @param key The key for which to return the corresponding configuration value.
 
 @return The value associated with `key`, or `nil` if there is no such value.
 */
- (nullable id)objectForKey:(NSString *)key;

/**
 Returns the object associated with a given key.
 
 This method enables usage of literal syntax on `BEConfig`.
 E.g. `NSString *value = config[@"key"];`
 
 @see objectForKey:
 
 @param keyedSubscript The keyed subscript for which to return the corresponding configuration value.
 
 @return The value associated with `key`, or `nil` if there is no such value.
 */
- (nullable id)objectForKeyedSubscript:(NSString *)keyedSubscript;

@end
