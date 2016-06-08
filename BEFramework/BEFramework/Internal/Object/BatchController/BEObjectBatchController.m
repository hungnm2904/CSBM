//
//  BEObjectBatchController.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEObjectBatchController.h"

#import <Bolts/Bolts.h>
#import "BFTask+Private.h"
#import "BEAssert.h"
#import "BECommandResult.h"
#import "BECommandRunning.h"
#import "BEErrorUtilities.h"
#import "BEMacros.h"
#import "BEObjectController.h"
#import "BEObjectPrivate.h"
#import "BEQueryPrivate.h"
#import "BERESTQueryCommand.h"
#import "BERESTQueryCommand.h"
#import "BERESTObjectCommand.h"
#import "BERESTObjectBatchCommand.h"
#import "BEQuery.h"

@implementation BEObjectBatchController

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
#pragma mark - Fetch
///--------------------------------------

- (BFTask *)fetchObjectsAsync:(NSArray *)objects withSessionToken:(NSString *)sessionToken {
  if (objects.count == 0) {
    return [BFTask taskWithResult:objects];
  }
  
  @weakify(self);
  return [[BFTask taskFromExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:^id{
    @strongify(self);
    BERESTCommand *command = [self _fetchCommandForObjects:objects withSessionToken:sessionToken];
    return [self.dataSource.commandRunner runCommandAsync:command
                                              withOptions:BECommandRunningOptionsRetryIfFailed];
  }] continueWithSuccessBlock:^id(BFTask *task) {
    @strongify(self);
    BECommandResult *result = task.result;
    return [self _processFetchResultAsync:result.result forObjects:objects];
  }];
}

- (BERESTCommand *)_fetchCommandForObjects:(NSArray *)objects withSessionToken:(NSString *)sessionToken {
  NSArray *objectIds = [objects valueForKey:@keypath(BEObject, objectId)];
  BEQuery *query = [BEQuery queryWithClassName:[objects.firstObject csbmClassName]];
  [query whereKey:@keypath(BEObject, objectId) containedIn:objectIds];
  query.limit = objectIds.count;
  return [BERESTQueryCommand findCommandForQueryState:query.state withSessionToken:sessionToken];
}

- (BFTask *)_processFetchResultAsync:(NSDictionary *)result forObjects:(NSArray *)objects {
  return [BFTask taskFromExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:^id{
    NSArray *results = result[@"results"]; // TODO: (nlutsenko) Move this logic into command itself?
    NSArray *objectIds = [results valueForKey:@keypath(BEObject, objectId)];
    NSDictionary *objectResults = [NSDictionary dictionaryWithObjects:results forKeys:objectIds];
    
    NSMutableArray *tasks = [NSMutableArray arrayWithCapacity:objects.count];
    for (BEObject *object in objects) {
      BEObjectController *controller = [[object class] objectController];
      NSDictionary *objectResult = objectResults[object.objectId];
      
      BFTask *task = nil;
      if (objectResult) {
        task = [controller processFetchResultAsync:objectResult forObject:object];
      } else {
        NSError *error = [BEErrorUtilities errorWithCode:kBEErrorObjectNotFound
                                                 message:@"Object not found on the server."];
        task = [BFTask taskWithError:error];
      }
      [tasks addObject:task];
    }
    return [BFTask taskForCompletionOfAllTasks:tasks];
  }];
}

///--------------------------------------
#pragma mark - Delete
///--------------------------------------

- (BFTask *)deleteObjectsAsync:(NSArray *)objects withSessionToken:(NSString *)sessionToken {
  if (objects.count == 0) {
    return [BFTask taskWithResult:objects];
  }
  
  @weakify(self);
  return [[BFTask taskFromExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:^id{
    @strongify(self);
    NSArray *objectBatches = [BEInternalUtils arrayBySplittingArray:objects
                                    withMaximumComponentsPerSegment:BERESTObjectBatchCommandSubcommandsLimit];
    NSMutableArray *tasks = [NSMutableArray arrayWithCapacity:objectBatches.count];
    
    id<BECommandRunning> commandRunner = self.dataSource.commandRunner;
    NSURL *serverURL = commandRunner.serverURL;
    for (NSArray *batch in objectBatches) {
      
      BERESTCommand *command = [self _deleteCommandForObjects:batch withSessionToken:sessionToken serverURL:serverURL];
      BFTask *task = [[commandRunner runCommandAsync:command
                                         withOptions:BECommandRunningOptionsRetryIfFailed] continueWithSuccessBlock:^id(BFTask *task) {
        BECommandResult *result = task.result;
        return [self _processDeleteResultsAsync:result.result forObjects:batch];
      }];
      [tasks addObject:task];
    }
    return [[BFTask taskForCompletionOfAllTasks:tasks] continueWithBlock:^id(BFTask *task) {
      NSError *taskError = task.error;
      if (taskError && [taskError.domain isEqualToString:BFTaskErrorDomain]) {
        NSArray *taskErrors = taskError.userInfo[@"errors"];
        NSMutableArray *errors = [NSMutableArray array];
        for (NSError *error in taskErrors) {
          if ([error.domain isEqualToString:BFTaskErrorDomain]) {
            [errors addObjectsFromArray:error.userInfo[@"errors"]];
          } else {
            [errors addObject:error];
          }
        }
        return [BFTask taskWithError:[NSError errorWithDomain:BFTaskErrorDomain
                                                         code:kBFMultipleErrorsError
                                                     userInfo:@{ @"errors" : errors }]];
      }
      return task;
    }];
  }] continueWithSuccessResult:objects];
}

- (BERESTCommand *)_deleteCommandForObjects:(NSArray *)objects
                           withSessionToken:(NSString *)sessionToken
                                  serverURL:(NSURL *)serverURL {
  NSMutableArray *commands = [NSMutableArray arrayWithCapacity:objects.count];
  for (BEObject *object in objects) {
    BERESTCommand *deleteCommand = [BERESTObjectCommand deleteObjectCommandForObjectState:object._state
                                                                         withSessionToken:sessionToken];
    [commands addObject:deleteCommand];
  }
  return [BERESTObjectBatchCommand batchCommandWithCommands:commands sessionToken:sessionToken serverURL:serverURL];
}

- (BFTask *)_processDeleteResultsAsync:(NSArray *)results forObjects:(NSArray *)objects {
  NSMutableArray *tasks = [NSMutableArray arrayWithCapacity:results.count];
  [results enumerateObjectsUsingBlock:^(NSDictionary *result, NSUInteger idx, BOOL *stop) {
    BEObject *object = objects[idx];
    NSDictionary *errorResult = result[@"error"];
    NSDictionary *successResult = result[@"success"];
    
    id<BEObjectControlling> controller = [[object class] objectController];
    BFTask *task = [controller processDeleteResultAsync:successResult forObject:object];
    if (errorResult) {
      task = [task continueWithBlock:^id(BFTask *task) {
        return [BFTask taskWithError:[BEErrorUtilities errorFromResult:errorResult]];
      }];
    }
    [tasks addObject:task];
  }];
  return [BFTask taskForCompletionOfAllTasks:tasks];
}

///--------------------------------------
#pragma mark - Utilities
///--------------------------------------

//TODO: (nlutsenko) Convert to use `uniqueObjectsArrayFromArray:usingFilter:`
+ (NSArray *)uniqueObjectsArrayFromArray:(NSArray *)objects omitObjectsWithData:(BOOL)omitFetched {
  if (objects.count == 0) {
    return objects;
  }
  
  NSMutableSet *set = [NSMutableSet setWithCapacity:objects.count];
  NSString *className = [objects.firstObject csbmClassName];
  for (BEObject *object in objects) {
    @synchronized (object.lock) {
      if (omitFetched && object.dataAvailable) {
        continue;
      }
      
      //TODO: (nlutsenko) Convert to using errors instead of assertions.
      BEParameterAssert([className isEqualToString:object.parseClassName],
                        @"All object should be in the same class.");
      BEParameterAssert(object.objectId != nil,
                        @"All objects must exist on the server.");
      
      [set addObject:object];
    }
  }
  return set.allObjects;
}

+ (NSArray *)uniqueObjectsArrayFromArray:(NSArray *)objects usingFilter:(BOOL (^)(BEObject *object))filter {
  if (objects.count == 0) {
    return objects;
  }
  
  NSMutableDictionary *uniqueObjects = [NSMutableDictionary dictionary];
  for (BEObject *object in objects) {
    if (!filter(object)) {
      continue;
    }
    
    // Use stringWithFormat: in case objectId or parseClassName are nil.
    NSString *objectIdentifier = [NSString stringWithFormat:@"%@%@", object.parseClassName, object.objectId];
    if (!uniqueObjects[objectIdentifier]) {
      uniqueObjects[objectIdentifier] = object;
    }
  }
  return uniqueObjects.allValues;
}

@end
