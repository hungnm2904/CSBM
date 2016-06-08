//
//  BEAnonymousUtils.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEConstants.h"
#import "BEUser.h"

@interface BEAnonymousUtils : NSObject

///--------------------------------------
#pragma mark - Creating an Anonymous User
///--------------------------------------

/**
 Creates an anonymous user asynchronously and sets as a result to `BFTask`.
 
 @return The task, that encapsulates the work being done.
 */
+ (BFTask<BEUser *> *)logInInBackground;

/**
 Creates an anonymous user asynchronously and performs a provided block.
 
 @param block The block to execute when anonymous user creation is complete.
 It should have the following argument signature: `^(PFUser *user, NSError *error)`.
 */
+ (void)logInWithBlock:(nullable BEUserResultBlock)block;

///--------------------------------------
#pragma mark - Determining Whether a User is Anonymous
///--------------------------------------

/**
 Whether the `PFUser` object is logged in anonymously.
 
 @param user `PFUser` object to check for anonymity. The user must be logged in on this device.
 
 @return `YES` if the user is anonymous. `NO` if the user is not the current user or is not anonymous.
 */
+ (BOOL)isLinkedWithUser:(nullable BEUser *)user;

@end
