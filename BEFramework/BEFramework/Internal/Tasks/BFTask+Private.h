//
//  BFTask+Private.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <Bolts/BFExecutor.h>
#import <Bolts/BFTask.h>
#import "BEInternalUtils.h"

@interface BFExecutor (Background)
+ (instancetype)defaultPriorityBackgroundExecutor;

@end
@interface BFTask (Private)

- (BFTask *)continueAsyncWithBlock:(BFContinuationBlock)block;
- (BFTask *)continueAsyncWithSuccessBlock:(BFContinuationBlock)block;

- (BFTask *)continueImmediatelyWithBlock:(BFContinuationBlock)block;
- (BFTask *)continueImmediatelyWithSuccessBlock:(BFContinuationBlock)block;

- (BFTask *)continueWithResult:(id)result;
- (BFTask *)continueWithSuccessResult:(id)result;

- (BFTask *)continueWithMainThreadResultBlock:(BEIdResultBlock)resultBlock
                           executeIfCancelled:(BOOL)executeIfCancelled;
- (BFTask *)continueWithMainThreadBooleanResultBlock:(BEBooleanResultBlock)resultBlock
                                  executeIfCancelled:(BOOL)executeIfCancelled;

/**
 Adds a continuation to the task that will run the given block on the main
 thread sometime after this task has finished. If the task was cancelled,
 the block will never be called. If the task had an exception, the exception
 will be throw on the main thread instead of running the block. Otherwise,
 the block will be given the result and error of this task.
 @return A new task that will be finished once the block has run.
 */
- (BFTask *)thenCallBackOnMainThreadAsync:(void(^)(id result, NSError *error))block;

/**
 Identical to thenCallBackOnMainThreadAsync:, except that the result of a successful
 task will be converted to a BOOL using the boolValue method, and that will
 be passed to the block instead of the original result.
 */
- (BFTask *)thenCallBackOnMainThreadWithBoolValueAsync:(void(^)(BOOL result, NSError *error))block;

/**
 Same as `waitForResult:error withMainThreadWarning:YES`
 */
- (id)waitForResult:(NSError **)error;

/**
 Waits until this operation is completed, then returns its value.
 This method is inefficient and consumes a thread resource while its running.
 
 @param error          If an error occurs, upon return contains an `NSError` object that describes the problem.
 @param warningEnabled `BOOL` value that
 
 @return Returns a `self.result` if task completed. `nil` - if cancelled.
 */
- (id)waitForResult:(NSError **)error withMainThreadWarning:(BOOL)warningEnabled;

@end

extern void forceLoadCategory_BFTask_Private();
