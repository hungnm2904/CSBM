//
//  BEObjectController.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEObjectController.h"
#import "BEObjectController_Private.h"
#import "BFTask+Private.h"
#import "BEAssert.h"
#import "BECommandResult.h"
#import "BECommandRunning.h"
#import "BEErrorUtilities.h"
#import "BEMacros.h"
#import "BEObjectPrivate.h"
#import "BEObjectState.h"
#import "BERESTObjectCommand.h"
#import "BETaskQueue.h"
#import "BEDecoder.h"

@implementation BEObjectController
///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithDataSource:(id<BECommandRunnerProvider>)dataSource {
  self = [super init];
  if (!self) return nil;
  
  _dataSource = dataSource;
  
  return self;
}

+ (instancetype)controllerWithDataSource:(id<BECommandRunnerProvider>)dataSource {
  return [[self alloc] initWithDataSource:dataSource];
}

///--------------------------------------
#pragma mark - BEObjectControlling
///--------------------------------------

#pragma mark Fetch

- (BFTask *)fetchObjectAsync:(BEObject *)object withSessionToken:(NSString *)sessionToken {
  @weakify(self);
  return [[[[BFTask taskFromExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:^id{
    return [object _validateFetchAsync];
  }] continueWithSuccessBlock:^id(BFTask *task) {
    @strongify(self);
    BERESTCommand *command = [BERESTObjectCommand fetchObjectCommandForObjectState:[object._state copy]
                                                                  withSessionToken:sessionToken];
    return [self _runFetchCommand:command forObject:object];
  }] continueWithSuccessBlock:^id(BFTask *task) {
    @strongify(self);
    BECommandResult *result = task.result;
    return [self processFetchResultAsync:result.result forObject:object];
  }] continueWithSuccessResult:object];
}

- (BFTask *)_runFetchCommand:(BERESTCommand *)command forObject:(BEObject *)object {
  return [self.dataSource.commandRunner runCommandAsync:command withOptions:BECommandRunningOptionsRetryIfFailed];
}

- (BFTask *)processFetchResultAsync:(NSDictionary *)result forObject:(BEObject *)object {
  return [BFTask taskFromExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:^id{
    NSDictionary *fetchedObjects = [object _collectFetchedObjects];
    @synchronized (object.lock) {
      BEKnownParseObjectDecoder *decoder = [BEKnownParseObjectDecoder decoderWithFetchedObjects:fetchedObjects];
      [object _mergeAfterFetchWithResult:result decoder:decoder completeData:YES];
    }
    return nil;
  }];
}

#pragma mark Delete

- (BFTask *)deleteObjectAsync:(BEObject *)object withSessionToken:(nullable NSString *)sessionToken {
  @weakify(self);
  return [[BFTask taskFromExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:^id{
    return [object _validateDeleteAsync];
  }] continueWithSuccessBlock:^id(BFTask *task) {
    @strongify(self);
    BEObjectState *state = [object._state copy];
    if (!state.objectId) {
      return nil;
    }
    
    BERESTCommand *command = [BERESTObjectCommand deleteObjectCommandForObjectState:state
                                                                   withSessionToken:sessionToken];
    return [[self _runDeleteCommand:command forObject:object] continueWithBlock:^id(BFTask *fetchTask) {
      @strongify(self);
      BECommandResult *result = fetchTask.result;
      return [[self processDeleteResultAsync:result.result forObject:object] continueWithBlock:^id(BFTask *task) {
        // Propagate the result of network task if it's faulted, cancelled.
        if (fetchTask.faulted || fetchTask.cancelled) {
          return fetchTask;
        }
        // Propagate the result of processDeleteResult otherwise.
        return task;
      }];
    }];
  }];
}

- (BFTask *)_runDeleteCommand:(BERESTCommand *)command forObject:(BEObject *)object {
  return [self.dataSource.commandRunner runCommandAsync:command withOptions:BECommandRunningOptionsRetryIfFailed];
}

- (BFTask *)processDeleteResultAsync:(NSDictionary *)result forObject:(BEObject *)object {
  return [BFTask taskFromExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:^id{
    BOOL deleted = (result != nil);
    [object _setDeleted:deleted];
    return nil;
  }];
}
@end
