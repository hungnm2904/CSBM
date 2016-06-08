//
//  BEUserAuthenticationController.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEUserAuthenticationController.h"

#import "BFTask+Private.h"
#import "BEMacros.h"
#import "BEUserPrivate.h"
#import "BEAnonymousUtils.h"
#import "BEAnonymousAuthenticationProvider.h"
#import "BEUserController.h"
#import "BECurrentUserController.h"
#import "BEAssert.h"

#import "BEUser.h"

@interface BEUserAuthenticationController () {
  dispatch_queue_t _dataAccessQueue;
  NSMutableDictionary<NSString *, id<BEUserAuthenticationDelegate>>*_authenticationDelegates;
}

@end

@implementation BEUserAuthenticationController

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithDataSource:(id<BECurrentUserControllerProvider, BEUserControllerProvider>)dataSource {
  self = [super init];
  if (!self) return nil;
  
  _dataSource = dataSource;
  _dataAccessQueue = dispatch_queue_create("com.parse.user.authenticationManager", DISPATCH_QUEUE_SERIAL);
  _authenticationDelegates = [NSMutableDictionary dictionary];
  
  return self;
}

+ (instancetype)controllerWithDataSource:(id<BECurrentUserControllerProvider, BEUserControllerProvider>)dataSource {
  return [[self alloc] initWithDataSource:dataSource];
}

///--------------------------------------
#pragma mark - Authentication Providers
///--------------------------------------

- (void)registerAuthenticationDelegate:(id<BEUserAuthenticationDelegate>)delegate forAuthType:(NSString *)authType {
  BEParameterAssert(delegate, @"Authentication delegate can't be `nil`.");
  BEParameterAssert(authType, @"`authType` can't be `nil`.");
  BEConsistencyAssert(![self authenticationDelegateForAuthType:authType],
                      @"Authentication delegate already registered for authType `%@`.", authType);
  
  dispatch_sync(_dataAccessQueue, ^{
    _authenticationDelegates[authType] = delegate;
  });
  
  // TODO: (nlutsenko) Decouple this further.
  [[self.dataSource.currentUserController getCurrentUserAsyncWithOptions:0] continueWithSuccessBlock:^id(BFTask *task) {
    BEUser *user = task.result;
    [user synchronizeAuthDataWithAuthType:authType];
    return nil;
  }];
}

- (void)unregisterAuthenticationDelegateForAuthType:(NSString *)authType {
  if (!authType) {
    return;
  }
  dispatch_sync(_dataAccessQueue, ^{
    [_authenticationDelegates removeObjectForKey:authType];
  });
}

- (id<BEUserAuthenticationDelegate>)authenticationDelegateForAuthType:(NSString *)authType {
  if (!authType) {
    return nil;
  }
  
  __block id<BEUserAuthenticationDelegate> delegate = nil;
  dispatch_sync(_dataAccessQueue, ^{
    delegate = _authenticationDelegates[authType];
  });
  return delegate;
}

///--------------------------------------
#pragma mark - Authentication
///--------------------------------------

- (BFTask<NSNumber *> *)restoreAuthenticationAsyncWithAuthData:(nullable NSDictionary<NSString *, NSString *> *)authData
                                                   forAuthType:(NSString *)authType {
  id<BEUserAuthenticationDelegate> provider = [self authenticationDelegateForAuthType:authType];
  if (!provider) {
    return [BFTask taskWithResult:@YES];
  }
  return [BFTask taskFromExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:^id {
    return @([provider restoreAuthenticationWithAuthData:authData]);
  }];
}

- (BFTask<NSNumber *> *)deauthenticateAsyncWithAuthType:(NSString *)authType {
  return [self restoreAuthenticationAsyncWithAuthData:nil forAuthType:authType];
}

///--------------------------------------
#pragma mark - Log In
///--------------------------------------

- (BFTask<BEUser *> *)logInUserAsyncWithAuthType:(NSString *)authType
                                        authData:(NSDictionary<NSString *, NSString *> *)authData {
  return [[self.dataSource.currentUserController getCurrentUserAsyncWithOptions:0] continueWithSuccessBlock:^id(BFTask<BEUser *> *task) {
    BEUser *currentUser = task.result;
    if (currentUser && [BEAnonymousUtils isLinkedWithUser:currentUser]) {
      if (currentUser._lazy) {
//        BFTask *resolveLaziness = nil;
//        NSDictionary *oldAnonymousData = nil;
//        @synchronized(currentUser.lock) {
//          oldAnonymousData = currentUser.authData[BEAnonymousUserAuthenticationType];
//          
//          // Replace any anonymity with the new linked authData
//          [currentUser stripAnonymity];
//          
//          currentUser.authData[authType] = authData;
//          [currentUser.linkedServiceNames addObject:authType];
//          
//          resolveLaziness = [currentUser resolveLazinessAsync:[BFTask taskWithResult:nil]];
//        }
//        return [resolveLaziness continueWithBlock:^id(BFTask *task) {
//          if (task.cancelled || task.faulted) {
//            [currentUser.authData removeObjectForKey:authType];
//            [currentUser.linkedServiceNames removeObject:authType];
//            [currentUser restoreAnonymity:oldAnonymousData];
//            return task;
//          }
//          return task.result;
//        }];
      } else {
        return [[currentUser linkWithAuthTypeInBackground:authType authData:authData] continueWithBlock:^id(BFTask *task) {
          NSError *error = task.error;
          if (error) {
            if (error.code == kBEErrorAccountAlreadyLinked) {
              // An account that's linked to the given authData already exists,
              // so log in instead of trying to claim.
              return [self.dataSource.userController logInCurrentUserAsyncWithAuthType:authType
                                                                              authData:authData
                                                                      revocableSession:[BEUser _isRevocableSessionEnabled]];
            } else {
              return task;
            }
          }
          return currentUser;
        }];
      }
    }
    return [self.dataSource.userController logInCurrentUserAsyncWithAuthType:authType
                                                                    authData:authData
                                                            revocableSession:[BEUser _isRevocableSessionEnabled]];
  }];
}

@end
