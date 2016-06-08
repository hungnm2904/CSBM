//
//  BESession.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <Bolts/BFTask.h>
#import "BEObject.h"
#import "BESubclassing.h"

@class BESession;

typedef void(^BESessionResultBlock)(BESession *_Nullable session, NSError *_Nullable error);

/**
 `BESession` is a local representation of a session.
 This class is a subclass of a `BEObject`,
 and retains the same functionality as any other subclass of `BEObject`.
 */
@interface BESession : BEObject<BESubclassing>

/**
 The session token string for this session.
 */
@property (nullable, nonatomic, copy, readonly) NSString *sessionToken;

/**
 *Asynchronously* fetches a `BESession` object related to the current user.
 
 @return A task that is `completed` with an instance of `BESession` class or is `faulted` if the operation fails.
 */
+ (BFTask<BESession *> *)getCurrentSessionInBackground;

/**
 *Asynchronously* fetches a `BESession` object related to the current user.
 
 @param block The block to execute when the operation completes.
 It should have the following argument signature: `^(BESession *session, NSError *error)`.
 */
+ (void)getCurrentSessionInBackgroundWithBlock:(nullable BESessionResultBlock)block;

@end
