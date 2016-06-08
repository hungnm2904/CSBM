//
//  BEConfigController.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEConfigController.h"

#import "BFTask+Private.h"
#import "BEAssert.h"
#import "BECommandResult.h"
#import "BECommandRunning.h"
#import "BEDecoder.h"
#import "BEConfig_Private.h"
#import "BECurrentConfigController.h"
#import "BERESTConfigCommand.h"

@interface BEConfigController () {
  dispatch_queue_t _dataAccessQueue;
  dispatch_queue_t _networkQueue;
  BFExecutor *_networkExecutor;
}

@end

@implementation BEConfigController

@synthesize currentConfigController = _currentConfigController;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithDataSource:(id<BEPersistenceControllerProvider, BECommandRunnerProvider>)dataSource {
  self = [super init];
  if (!self) return nil;
  
  _dataSource = dataSource;
  
  _dataAccessQueue = dispatch_queue_create("com.parse.config.access", DISPATCH_QUEUE_SERIAL);
  
  _networkQueue = dispatch_queue_create("com.parse.config.network", DISPATCH_QUEUE_SERIAL);
  _networkExecutor = [BFExecutor executorWithDispatchQueue:_networkQueue];
  
  return self;
}

///--------------------------------------
#pragma mark - Fetch
///--------------------------------------

- (BFTask *)fetchConfigAsyncWithSessionToken:(NSString *)sessionToken {
  @weakify(self);
  return [BFTask taskFromExecutor:_networkExecutor withBlock:^id {
    @strongify(self);
    BERESTCommand *command = [BERESTConfigCommand configFetchCommandWithSessionToken:sessionToken];
    return [[[self.dataSource.commandRunner runCommandAsync:command
                                                withOptions:BECommandRunningOptionsRetryIfFailed]
             continueWithSuccessBlock:^id(BFTask *task) {
               BECommandResult *result = task.result;
               NSDictionary *fetchedConfig = [[BEDecoder objectDecoder] decodeObject:result.result];
               return [[BEConfig alloc] initWithFetchedConfig:fetchedConfig];
             }] continueWithSuccessBlock:^id(BFTask *task) {
               // Roll-forward the config.
               return [[self.currentConfigController setCurrentConfigAsync:task.result] continueWithResult:task.result];
             }];
  }];
}

///--------------------------------------
#pragma mark - Accessors
///--------------------------------------

- (BECurrentConfigController *)currentConfigController {
  __block BECurrentConfigController *controller = nil;
  dispatch_sync(_dataAccessQueue, ^{
    if (!_currentConfigController) {
      _currentConfigController = [[BECurrentConfigController alloc] initWithDataSource:self.dataSource];
    }
    controller = _currentConfigController;
  });
  return controller;
}

@end