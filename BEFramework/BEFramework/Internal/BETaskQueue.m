//
//  BETaskQueue.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BETaskQueue.h"
#import <Bolts/BFTask.h>

@interface BETaskQueue()

@property (nonatomic, strong, readwrite) BFTask *tail;
@property (nonatomic, strong, readwrite) NSObject *mutex;

@end
@implementation BETaskQueue

- (instancetype)init {
  self = [super init];
  if (!self) return nil;
  
  self.mutex = [[NSObject alloc] init];
  
  return self;
}

- (BFTask *)enqueue:(BFTask *(^)(BFTask *toAwait))taskStart {
  @synchronized (self.mutex) {
    BFTask *oldTail = self.tail ?: [BFTask taskWithResult:nil];
    
    // The task created by taskStart is responsible for waiting on the
    // task passed to it before doing its work. This gives it an opportunity
    // to do startup work or save state before waiting for its turn in the queue.
    BFTask *task = taskStart(oldTail);
    
    // The tail task should be dependent on the old tail as well as the newly-created
    // task. This prevents cancellation of the new task from causing the queue to run
    // out of order.
    self.tail = [BFTask taskForCompletionOfAllTasks:@[oldTail, task]];
    
    return task;
  }
}

@end
