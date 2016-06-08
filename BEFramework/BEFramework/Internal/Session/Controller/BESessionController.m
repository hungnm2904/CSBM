//
//  BESessionController.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BESessionController.h"
#import "BFTask+Private.h"
#import "BEAssert.h"
#import "BECommandResult.h"
#import "BECommandRunning.h"
#import "BEObjectPrivate.h"

#import "BERESTSessionCommand.h"
#import "BESession.h"

@implementation BESessionController

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
#pragma mark - Current Session
///--------------------------------------

- (BFTask *)getCurrentSessionAsyncWithSessionToken:(NSString *)sessionToken {
  @weakify(self);
  return [[BFTask taskFromExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:^id{
    @strongify(self);
    BERESTCommand *command = [BERESTSessionCommand getCurrentSessionCommandWithSessionToken:sessionToken];
    return [self.dataSource.commandRunner runCommandAsync:command
                                              withOptions:BECommandRunningOptionsRetryIfFailed];
  }] continueWithSuccessBlock:^id(BFTask *task) {
    BECommandResult *result = task.result;
    NSDictionary *dictionary = result.result;
    BESession *session = [BESession _objectFromDictionary:dictionary
                                         defaultClassName:[BESession csbmClassName]
                                             completeData:YES];
    return session;
  }];
}

@end
