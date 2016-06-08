//
//  BEUserController.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEUserController.h"
#import "BFTask+Private.h"
#import "BECommandResult.h"
#import "BECommandRunning.h"
#import "BECurrentUserController.h"
#import "BEErrorUtilities.h"
#import "BEMacros.h"
#import "BEObjectPrivate.h"
#import "BERESTUserCommand.h"
#import "BEUserPrivate.h"
#import "BEMutableUserState.h"

@implementation BEUserController

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithCommonDataSource:(id<BECommandRunnerProvider>)commonDataSource
                          coreDataSource:(id<BECurrentUserControllerProvider>)coreDataSource {
  self = [super init];
  if (!self) return nil;
  
  _commonDataSource = commonDataSource;
  _coreDataSource = coreDataSource;
  
  return self;
}

+ (instancetype)controllerWithCommonDataSource:(id<BECommandRunnerProvider>)commonDataSource
                                coreDataSource:(id<BECurrentUserControllerProvider>)coreDataSource {
  return [[self alloc] initWithCommonDataSource:commonDataSource
                                 coreDataSource:coreDataSource];
}

///--------------------------------------
#pragma mark - Log In
///--------------------------------------

- (BFTask *)logInCurrentUserAsyncWithSessionToken:(NSString *)sessionToken {
  @weakify(self);
  return [[BFTask taskFromExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:^id{
    @strongify(self);
    BERESTCommand *command = [BERESTUserCommand getCurrentUserCommandWithSessionToken:sessionToken];
    return [self.commonDataSource.commandRunner runCommandAsync:command
                                                    withOptions:BECommandRunningOptionsRetryIfFailed];
  }] continueWithSuccessBlock:^id(BFTask *task) {
    @strongify(self);
    BECommandResult *result = task.result;
    NSDictionary *dictionary = result.result;
    
    // We test for a null object, if it isn't, we can use the response to create a BEUser.
    if ([dictionary isKindOfClass:[NSNull class]] || !dictionary) {
      return [BFTask taskWithError:[BEErrorUtilities errorWithCode:kBEErrorObjectNotFound
                                                           message:@"Invalid Session Token."]];
    }
    
    BEUser *user = [BEUser _objectFromDictionary:dictionary
                                defaultClassName:[BEUser csbmClassName]
                                    completeData:YES];
    // Serialize the object to disk so we can later access it via currentUser
    BECurrentUserController *controller = self.coreDataSource.currentUserController;
    return [[controller saveCurrentObjectAsync:user] continueWithBlock:^id(BFTask *task) {
      return user;
    }];
  }];
}

- (BFTask *)logInCurrentUserAsyncWithUsername:(NSString *)username
                                     password:(NSString *)password
                             revocableSession:(BOOL)revocableSession {
  @weakify(self);
  return [[BFTask taskFromExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:^id{
    BERESTCommand *command = [BERESTUserCommand logInUserCommandWithUsername:username
                                                                    password:password
                                                            revocableSession:revocableSession];
    return [self.commonDataSource.commandRunner runCommandAsync:command
                                                    withOptions:BECommandRunningOptionsRetryIfFailed];
  }] continueWithSuccessBlock:^id(BFTask *task) {
    @strongify(self);
    BECommandResult *result = task.result;
    NSDictionary *dictionary = result.result;
    
    // We test for a null object, if it isn't, we can use the response to create a BEUser.
    if ([dictionary isKindOfClass:[NSNull class]] || !dictionary) {
      return [BFTask taskWithError:[BEErrorUtilities errorWithCode:kBEErrorObjectNotFound
                                                           message:@"Invalid login credentials."]];
    }
    
    BEUser *user = [BEUser _objectFromDictionary:dictionary
                                defaultClassName:[BEUser csbmClassName]
                                    completeData:YES];
    
    // Serialize the object to disk so we can later access it via currentUser
    BECurrentUserController *controller = self.coreDataSource.currentUserController;
    return [[controller saveCurrentObjectAsync:user] continueWithBlock:^id(BFTask *task) {
      return user;
    }];
  }];
}

//- (BFTask *)logInCurrentUserAsyncWithAuthType:(NSString *)authType
//                                     authData:(NSDictionary *)authData
//                             revocableSession:(BOOL)revocableSession {
//  @weakify(self);
//  return [[BFTask taskFromExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:^id{
//    @strongify(self);
//    BERESTCommand *command = [BERESTUserCommand serviceLoginUserCommandWithAuthenticationType:authType
//                                                                           authenticationData:authData
//                                                                             revocableSession:revocableSession];
//    return [self.commonDataSource.commandRunner runCommandAsync:command
//                                                    withOptions:BECommandRunningOptionsRetryIfFailed];
//  }] continueWithSuccessBlock:^id(BFTask *task) {
//    BECommandResult *result = task.result;
//    BEUser *user = [BEUser _objectFromDictionary:result.result
//                                defaultClassName:[BEUser csbmClassName]
//                                    completeData:YES];
//    @synchronized ([user lock]) {
//      user.authData[authType] = authData;
//      [user.linkedServiceNames addObject:authType];
//      [user startSave];
//      return [user _handleServiceLoginCommandResult:result];
//    }
//  }];
//}

///--------------------------------------
#pragma mark - Reset Password
///--------------------------------------

- (BFTask *)requestPasswordResetAsyncForEmail:(NSString *)email {
  @weakify(self);
  return [[BFTask taskFromExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:^id{
    @strongify(self);
    BERESTCommand *command = [BERESTUserCommand resetPasswordCommandForUserWithEmail:email];
    return [self.commonDataSource.commandRunner runCommandAsync:command
                                                    withOptions:BECommandRunningOptionsRetryIfFailed];
  }] continueWithSuccessResult:nil];
}

///--------------------------------------
#pragma mark - Log Out
///--------------------------------------

- (BFTask *)logOutUserAsyncWithSessionToken:(NSString *)sessionToken {
  @weakify(self);
  return [[BFTask taskFromExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:^id{
    @strongify(self);
    BERESTCommand *command = [BERESTUserCommand logOutUserCommandWithSessionToken:sessionToken];
    return [self.commonDataSource.commandRunner runCommandAsync:command
                                                    withOptions:BECommandRunningOptionsRetryIfFailed];
  }] continueWithSuccessResult:nil];
}
@end
