//
//  BEQueryController.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEQueryController.h"
#import <Bolts/BFCancellationToken.h>
#import "BFTask+Private.h"
#import "BEAssert.h"
#import "BECommandResult.h"
#import "BECommandRunning.h"
#import "BEObjectPrivate.h"
#import "BEQueryState.h"
#import "BERESTQueryCommand.h"
#import "BEUser.h"
#import "CSBM_Private.h"

@interface BEQueryController () <BEQueryControllerSubclass>

@end

@implementation BEQueryController

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithCommonDataSource:(id<BECommandRunnerProvider>)dataSource {
  self = [super init];
  if (!self) return nil;
  
  _commonDataSource = dataSource;
  
  return self;
}

+ (instancetype)controllerWithCommonDataSource:(id<BECommandRunnerProvider>)dataSource {
  return [[self alloc] initWithCommonDataSource:dataSource];
}

///--------------------------------------
#pragma mark - Find
///--------------------------------------

- (BFTask *)findObjectsAsyncForQueryState:(BEQueryState *)queryState
                    withCancellationToken:(BFCancellationToken *)cancellationToken
                                     user:(BEUser *)user {
  NSDate *queryStart = (queryState.trace ? [NSDate date] : nil);
  __block NSDate *querySent = nil;
  
  NSString *sessionToken = user.sessionToken;
  return [[BFTask taskFromExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:^id{
    if (cancellationToken.cancellationRequested) {
      return [BFTask cancelledTask];
    }
    
    BERESTCommand *command = [BERESTQueryCommand findCommandForQueryState:queryState withSessionToken:sessionToken];
    querySent = (queryState.trace ? [NSDate date] : nil);
    return [self runNetworkCommandAsync:command
                  withCancellationToken:cancellationToken
                          forQueryState:queryState];
  }] continueWithSuccessBlock:^id(BFTask *task) {
    BECommandResult *result = task.result;
    NSDate *queryReceived = (queryState.trace ? [NSDate date] : nil);
    
    NSArray *resultObjects = result.result[@"results"];
    NSMutableArray *foundObjects = [NSMutableArray arrayWithCapacity:resultObjects.count];
    if (resultObjects != nil) {
      NSString *resultClassName = result.result[@"className"];
      if (!resultClassName) {
        resultClassName = queryState.parseClassName;
      }
      NSArray *selectedKeys = queryState.selectedKeys.allObjects;
      for (NSDictionary *resultObject in resultObjects) {
        BEObject *object = [BEObject _objectFromDictionary:resultObject
                                          defaultClassName:resultClassName
                                              selectedKeys:selectedKeys];
        [foundObjects addObject:object];
      }
    }
    
    NSString *traceLog = result.result[@"trace"];
    if (traceLog != nil) {
      NSLog(@"Pre-processing took %f seconds\n%@Client side parsing took %f seconds",
            [querySent timeIntervalSinceDate:queryStart], traceLog,
            queryReceived.timeIntervalSinceNow);
    }
    
    return foundObjects;
  } cancellationToken:cancellationToken];
}

///--------------------------------------
#pragma mark - Count
///--------------------------------------

- (BFTask *)countObjectsAsyncForQueryState:(BEQueryState *)queryState
                     withCancellationToken:(BFCancellationToken *)cancellationToken
                                      user:(BEUser *)user {
  NSString *sessionToken = user.sessionToken;
  return [[BFTask taskFromExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:^id{
    if (cancellationToken.cancellationRequested) {
      return [BFTask cancelledTask];
    }
    
    BERESTQueryCommand *findCommand = [BERESTQueryCommand findCommandForQueryState:queryState
                                                                  withSessionToken:sessionToken];
    BERESTCommand *countCommand = [BERESTQueryCommand countCommandFromFindCommand:findCommand];
    return [self runNetworkCommandAsync:countCommand
                  withCancellationToken:cancellationToken
                          forQueryState:queryState];
  }] continueWithSuccessBlock:^id(BFTask *task) {
    BECommandResult *result = task.result;
    return result.result[@"count"];
  } cancellationToken:cancellationToken];
}

///--------------------------------------
#pragma mark - Caching
///--------------------------------------

- (NSString *)cacheKeyForQueryState:(BEQueryState *)queryState sessionToken:(NSString *)sessionToken {
  return nil;
}

- (BOOL)hasCachedResultForQueryState:(BEQueryState *)queryState sessionToken:(NSString *)sessionToken {
  return NO;
}

- (void)clearCachedResultForQueryState:(BEQueryState *)queryState sessionToken:(NSString *)sessionToken {
}

- (void)clearAllCachedResults {
}

///--------------------------------------
#pragma mark - BEQueryControllerSubclass
///--------------------------------------

- (BFTask *)runNetworkCommandAsync:(BERESTCommand *)command
             withCancellationToken:(BFCancellationToken *)cancellationToken
                     forQueryState:(BEQueryState *)queryState {
  return [self.commonDataSource.commandRunner runCommandAsync:command
                                                  withOptions:BECommandRunningOptionsRetryIfFailed
                                            cancellationToken:cancellationToken];
}

@end
