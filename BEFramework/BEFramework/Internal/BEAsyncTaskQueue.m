//
//  BEAsyncTaskQueue.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEAsyncTaskQueue.h"
#import <Bolts/BFTaskCompletionSource.h>
#import "BFTask+Private.h"

@interface BEAsyncTaskQueue()

@property (nonatomic, strong) dispatch_queue_t syncQueue;
@property (nonatomic, strong) BFTask *tail;

@end

@implementation BEAsyncTaskQueue

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init {
  self = [super init];
  if (!self) return nil;
  
  _tail = [BFTask taskWithResult:nil];
  _syncQueue = dispatch_queue_create("com.parse.asynctaskqueue.sync", DISPATCH_QUEUE_SERIAL);
  
  return self;
}

+ (instancetype)taskQueue {
  return [[self alloc] init];
}

///--------------------------------------
#pragma mark - Enqueue
///--------------------------------------

- (BFTask *)enqueue:(BFContinuationBlock)block {
  BFTaskCompletionSource *source = [BFTaskCompletionSource taskCompletionSource];
  dispatch_async(_syncQueue, ^{
    _tail = [_tail continueAsyncWithBlock:block];
    [_tail continueAsyncWithBlock:^id(BFTask *task) {
      if (task.faulted) {
        NSError *error = task.error;
        if (error) {
          [source trySetError:error];
        } else {
          [source trySetException:task.exception];
        }
      } else if (task.cancelled) {
        [source trySetCancelled];
      } else {
        [source trySetResult:task.result];
      }
      return task;
    }];
  });
  return source.task;
}

@end
