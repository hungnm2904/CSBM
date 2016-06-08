//
//  BEUserPrivate.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEUser.h"
#import "BEMacros.h"

extern NSString *const BEUserCurrentUserFileName;
extern NSString *const BEUserCurrentUserPinName;
extern NSString *const BEUserCurrentUserKeychainItemName;

@class BFTask<__covariant BFGenericType>;
@class BECommandResult;
@class BEUserController;

@interface BEUser (Private)

///--------------------------------------
#pragma mark - Current User
///--------------------------------------
+ (BFTask *)_getCurrentUserSessionTokenAsync;
+ (NSString *)currentSessionToken;

- (void)synchronizeAllAuthData;

- (BFTask *)_handleServiceLoginCommandResult:(BECommandResult *)result;

- (void)synchronizeAuthDataWithAuthType:(NSString *)authType;

+ (BEUser *)logInLazyUserWithAuthType:(NSString *)authType authData:(NSDictionary *)authData;
- (BFTask *)resolveLazinessAsync:(BFTask *)toAwait;
- (void)stripAnonymity;
- (void)restoreAnonymity:(id)data;

///--------------------------------------
#pragma mark - Revocable Session
///--------------------------------------
+ (BOOL)_isRevocableSessionEnabled;
+ (void)_setRevocableSessionEnabled:(BOOL)enabled;

+ (BEUserController *)userController;

@end

// Private Properties
@interface BEUser ()

@property (nonatomic, strong, readonly) NSMutableDictionary<NSString *, id> *authData;
@property (nonatomic, strong, readonly) NSMutableSet<NSString *> *linkedServiceNames;

/**
 This earmarks the user as being an "identity" user.
 This will make saves write through to the currentUser singleton and disk object
 */
@property (nonatomic, assign) BOOL _current;
@property (nonatomic, assign) BOOL _lazy;

- (BOOL)_isAuthenticatedWithCurrentUser:(BEUser *)currentUser;

- (BFTask *)_logOutAsync;

///--------------------------------------
#pragma mark - Third-party Authentication (Private)
///--------------------------------------

+ (void)_unregisterAuthenticationDelegateForAuthType:(NSString *)authType;

@end