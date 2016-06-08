//
//  BEAnonymousUtils.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEAnonymousUtils.h"
#import "BEAnonymousUtils_Private.h"
#import "BFTask+Private.h"
#import "BEInternalUtils.h"
#import "BEUserPrivate.h"
#import "BEAnonymousAuthenticationProvider.h"

@implementation BEAnonymousUtils

///--------------------------------------
#pragma mark - Log In
///--------------------------------------

+ (BFTask *)logInInBackground {
  BEAnonymousAuthenticationProvider *provider = [self _authenticationProvider];
  return [BEUser logInWithAuthTypeInBackground:BEAnonymousUserAuthenticationType authData:provider.authData];
}

+ (void)logInWithBlock:(BEUserResultBlock)block {
  [[self logInInBackground] thenCallBackOnMainThreadAsync:block];
}

///--------------------------------------
#pragma mark - Link
///--------------------------------------

+ (BOOL)isLinkedWithUser:(BEUser *)user {
  return [user isLinkedWithAuthType:BEAnonymousUserAuthenticationType];
}

///--------------------------------------
#pragma mark - Private
///--------------------------------------

static BEAnonymousAuthenticationProvider *authenticationProvider_;

+ (dispatch_queue_t)_providerAccessQueue {
  static dispatch_queue_t queue;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    queue = dispatch_queue_create("com.parse.anonymousUtils.provider.access", DISPATCH_QUEUE_SERIAL);
  });
  return queue;
}

+ (BEAnonymousAuthenticationProvider *)_authenticationProvider {
  __block BEAnonymousAuthenticationProvider *provider = nil;
  dispatch_sync([self _providerAccessQueue], ^{
    provider = authenticationProvider_;
    if (!provider) {
      provider = [[BEAnonymousAuthenticationProvider alloc] init];
      [BEUser registerAuthenticationDelegate:provider forAuthType:BEAnonymousUserAuthenticationType];
      authenticationProvider_ = provider;
    }
  });
  return provider;
}

//+ (void)_clearAuthenticationProvider {
//  [BEUser _unregisterAuthenticationDelegateForAuthType:BEAnonymousUserAuthenticationType];
//  dispatch_sync([self _providerAccessQueue], ^{
//    authenticationProvider_ = nil;
//  });
//}
//
/////--------------------------------------
//#pragma mark - Lazy Login
/////--------------------------------------
//
//+ (BEUser *)_lazyLogIn {
//  BEAnonymousAuthenticationProvider *provider = [self _authenticationProvider];
//  return [BEUser logInLazyUserWithAuthType:BEAnonymousUserAuthenticationType authData:provider.authData];
//}

@end

@implementation BEAnonymousUtils (Deprecated)

+ (void)logInWithTarget:(nullable id)target selector:(nullable SEL)selector {
  [self logInWithBlock:^(BEUser *user, NSError *error) {
    [BEInternalUtils safePerformSelector:selector withTarget:target object:user object:error];
  }];
}

@end
