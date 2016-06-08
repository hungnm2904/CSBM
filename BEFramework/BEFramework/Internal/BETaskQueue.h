//
//  BETaskQueue.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEConstants.h"

@class BFTask<__covariant BFGenericType>;

@interface BETaskQueue : NSObject

// The lock for this task queue.
@property (nonatomic, strong, readonly) NSObject *mutex;

/**
 Enqueues a task created by the given block. Then block is given a task to
 await once state is snapshotted (e.g. after capturing session tokens at the
 time of the save call. Awaiting this task will wait for the created task's
 turn in the queue.
 */
- (BFTask *)enqueue:(BFTask *(^)(BFTask *toAwait))taskStart;

@end
