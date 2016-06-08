//
//  BFTask+Private.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BFTask+Private.h"
#import <Bolts/BFExecutor.h>
#import <Bolts/BFTaskCompletionSource.h>

@implementation BFExecutor (Background)

+ (instancetype)defaultPriorityBackgroundExecutor {
  static BFExecutor *executor;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    executor = [BFExecutor executorWithDispatchQueue:dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0)];
  });
  return executor;
}

@end

@implementation BFTask (Private)

- (BFTask *)continueAsyncWithBlock:(BFContinuationBlock)block {
  return [self continueWithExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:block];
}

- (BFTask *)continueAsyncWithSuccessBlock:(BFContinuationBlock)block {
  return [self continueWithExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withSuccessBlock:block];
}

- (BFTask *)continueImmediatelyWithBlock:(BFContinuationBlock)block {
  return [self continueWithExecutor:[BFExecutor immediateExecutor] withBlock:block];
}

- (BFTask *)continueImmediatelyWithSuccessBlock:(BFContinuationBlock)block {
  return [self continueWithExecutor:[BFExecutor immediateExecutor] withSuccessBlock:block];
}

- (BFTask *)continueWithResult:(id)result {
  return [self continueWithBlock:^id(BFTask *task) {
    return result;
  }];
}

- (BFTask *)continueWithSuccessResult:(id)result {
  return [self continueWithSuccessBlock:^id(BFTask *task) {
    return result;
  }];
}

- (BFTask *)continueWithMainThreadResultBlock:(BEIdResultBlock)resultBlock
                           executeIfCancelled:(BOOL)executeIfCancelled {
  if (!resultBlock) {
    return self;
  }
  return [self continueWithExecutor:[BFExecutor mainThreadExecutor]
                          withBlock:^id(BFTask *task) {
                            BFTaskCompletionSource *tcs = [BFTaskCompletionSource taskCompletionSource];
                            
                            @try {
                              if (self.exception) {
                                //TODO: (nlutsenko) Add more context, by passing a `_cmd` from the caller method
                                //BELogException(self.exception);
                                @throw self.exception;
                              }
                              
                              if (!self.cancelled || executeIfCancelled) {
                                resultBlock(self.result, self.error);
                              }
                            } @finally {
                              tcs.result = nil;
                            }
                            
                            return tcs.task;
                          }];
}

- (BFTask *)continueWithMainThreadBooleanResultBlock:(BEBooleanResultBlock)resultBlock
                                  executeIfCancelled:(BOOL)executeIfCancelled {
  return [self continueWithMainThreadResultBlock:^(id object, NSError *error) {
    resultBlock([object boolValue], error);
  } executeIfCancelled:executeIfCancelled];
}

- (BFTask *)thenCallBackOnMainThreadAsync:(void(^)(id result, NSError *error))block {
  return [self continueWithMainThreadResultBlock:block executeIfCancelled:NO];
}

- (BFTask *)thenCallBackOnMainThreadWithBoolValueAsync:(void(^)(BOOL result, NSError *error))block {
  if (!block) {
    return self;
  }
  return [self thenCallBackOnMainThreadAsync:^(id blockResult, NSError *blockError) {
    block([blockResult boolValue], blockError);
  }];
}

- (id)waitForResult:(NSError **)error {
  return [self waitForResult:error withMainThreadWarning:YES];
}

- (id)waitForResult:(NSError **)error withMainThreadWarning:(BOOL)warningEnabled {
  if (warningEnabled) {
    [self waitUntilFinished];
  } else {
    dispatch_semaphore_t semaphore = dispatch_semaphore_create(0);
    [self continueWithBlock:^id(BFTask *task) {
      dispatch_semaphore_signal(semaphore);
      return nil;
    }];
    dispatch_semaphore_wait(semaphore, DISPATCH_TIME_FOREVER);
  }
  if (self.cancelled) {
    return nil;
  } else if (self.exception) {
    @throw self.exception;
  }
  if (self.error && error) {
    *error = self.error;
  }
  return self.result;
}

@end
