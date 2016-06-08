//
//  BEUser.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <Bolts/BFTask.h>
#import "BEConstants.h"
#import "BEObject.h"
#import "BESubclassing.h"

typedef void(^BEUserSessionUpgradeResultBlock)(NSError *_Nullable error);
typedef void(^BEUserLogoutResultBlock)(NSError *_Nullable error);

@class BEQuery<BEGenericObject : BEObject *>;
@protocol BEUserAuthenticationDelegate;

@interface BEUser : BEObject <BESubclassing>

///--------------------------------------
#pragma mark - Accessing the Current User
///--------------------------------------

/**
 Gets the currently logged in user from disk and returns an instance of it.
 
 @return Returns a `BEUser` that is the currently logged in user. If there is none, returns `nil`.
 */
+ (nullable instancetype)currentUser;

/**
 *Asynchronously* loads the currently logged in user from disk and returns a task that encapsulates it.
 
 @return The task that encapsulates the work being done.
 */
+ (BFTask<__kindof BEUser *> *)getCurrentUserInBackground;

/**
 The session token for the `BEUser`.
 
 This is set by the server upon successful authentication.
 */
@property (nullable, nonatomic, copy, readonly) NSString *sessionToken;

/**
 Whether the `BEUser` was just created from a request.
 
 This is only set after a Facebook or Twitter login.
 */
@property (nonatomic, assign, readonly) BOOL isNew;

/**
 Whether the user is an authenticated object for the device.
 
 An authenticated `BEUser` is one that is obtained via a `-signUp:` or `+logInWithUsername:password:` method.
 An authenticated object is required in order to save (with altered values) or delete it.
 */
@property (nonatomic, assign, readonly, getter=isAuthenticated) BOOL authenticated;

///--------------------------------------
#pragma mark - Creating a New User
///--------------------------------------

/**
 Creates a new `BEUser` object.
 
 @return Returns a new `BEUser` object.
 */
+ (instancetype)user;

/**
 Enables automatic creation of anonymous users.
 
 After calling this method, `+currentUser` will always have a value.
 The user will only be created on the server once the user has been saved,
 or once an object with a relation to that user or an ACL that refers to the user has been saved.
 
 @warning `BEObject.-saveEventually` will not work on if an item being saved has a relation
 to an automatic user that has never been saved.
 */
+ (void)enableAutomaticUser;

/**
 The username for the `BEUser`.
 */
@property (nullable, nonatomic, strong) NSString *username;

/**!
 The password for the `BEUser`.
 
 This will not be filled in from the server with the password.
 It is only meant to be set.
 */
@property (nullable, nonatomic, strong) NSString *password;

/**
 The email for the `BEUser`.
 */
@property (nullable, nonatomic, strong) NSString *email;

/**
 Signs up the user *asynchronously*.
 
 This will also enforce that the username isn't already taken.
 
 @warning Make sure that password and username are set before calling this method.
 
 @return The task, that encapsulates the work being done.
 */
- (BFTask<NSNumber *> *)signUpInBackground;

/**
 Signs up the user *asynchronously*.
 
 This will also enforce that the username isn't already taken.
 
 @warning Make sure that password and username are set before calling this method.
 
 @param block The block to execute.
 It should have the following argument signature: `^(BOOL succeeded, NSError *error)`.
 */
- (void)signUpInBackgroundWithBlock:(nullable BEBooleanResultBlock)block;

///--------------------------------------
#pragma mark - Logging In
///--------------------------------------

/**
 Makes an *asynchronous* request to login a user with specified credentials.
 
 Returns an instance of the successfully logged in `BEUser`.
 This also caches the user locally so that calls to `+currentUser` will use the latest logged in user.
 
 @param username The username of the user.
 @param password The password of the user.
 
 @return The task, that encapsulates the work being done.
 */
+ (BFTask<__kindof BEUser *> *)logInWithUsernameInBackground:(NSString *)username password:(NSString *)password;

/**
 Makes an *asynchronous* request to log in a user with specified credentials.
 
 Returns an instance of the successfully logged in `BEUser`.
 This also caches the user locally so that calls to `+currentUser` will use the latest logged in user.
 
 @param username The username of the user.
 @param password The password of the user.
 @param block The block to execute.
 It should have the following argument signature: `^(BEUser *user, NSError *error)`.
 */
+ (void)logInWithUsernameInBackground:(NSString *)username password:(NSString *)password block:(nullable BEUserResultBlock)block;

///--------------------------------------
#pragma mark - Becoming a User
///--------------------------------------

/**
 Makes an *asynchronous* request to become a user with the given session token.
 
 Returns an instance of the successfully logged in `BEUser`.
 This also caches the user locally so that calls to `+currentUser` will use the latest logged in user.
 
 @param sessionToken The session token for the user.
 
 @return The task, that encapsulates the work being done.
 */
+ (BFTask<__kindof BEUser *> *)becomeInBackground:(NSString *)sessionToken;

/**
 Makes an *asynchronous* request to become a user with the given session token.
 
 Returns an instance of the successfully logged in `BEUser`. This also caches the user locally
 so that calls to `+currentUser` will use the latest logged in user.
 
 @param sessionToken The session token for the user.
 @param block The block to execute.
 The block should have the following argument signature: `^(BEUser *user, NSError *error)`.
 */
+ (void)becomeInBackground:(NSString *)sessionToken block:(nullable BEUserResultBlock)block;

///--------------------------------------
#pragma mark - Revocable Session
///--------------------------------------

/**
 Enables revocable sessions and migrates the currentUser session token to use revocable session if needed.
 
 This method is required if you want to use `BESession` APIs
 and your application's 'Require Revocable Session' setting is turned off on `http://parse.com` app settings.
 After returned `BFTask` completes - `BESession` class and APIs will be available for use.
 
 @return An instance of `BFTask` that is completed when revocable
 sessions are enabled and currentUser token is migrated.
 */
+ (BFTask *)enableRevocableSessionInBackground;

/**
 Enables revocable sessions and upgrades the currentUser session token to use revocable session if needed.
 
 This method is required if you want to use `BESession` APIs
 and legacy sessions are enabled in your application settings on `http://parse.com/`.
 After returned `BFTask` completes - `BESession` class and APIs will be available for use.
 
 @param block Block that will be called when revocable sessions are enabled and currentUser token is migrated.
 */
+ (void)enableRevocableSessionInBackgroundWithBlock:(nullable BEUserSessionUpgradeResultBlock)block;

///--------------------------------------
#pragma mark - Logging Out
///--------------------------------------

/**
 *Asynchronously* logs out the currently logged in user.
 
 This will also remove the session from disk, log out of linked services
 and all future calls to `+currentUser` will return `nil`. This is preferrable to using `-logOut`,
 unless your code is already running from a background thread.
 
 @return An instance of `BFTask`, that is resolved with `nil` result when logging out completes.
 */
+ (BFTask *)logOutInBackground;

/**
 *Asynchronously* logs out the currently logged in user.
 
 This will also remove the session from disk, log out of linked services
 and all future calls to `+currentUser` will return `nil`. This is preferrable to using `-logOut`,
 unless your code is already running from a background thread.
 
 @param block A block that will be called when logging out completes or fails.
 */
+ (void)logOutInBackgroundWithBlock:(nullable BEUserLogoutResultBlock)block;

///--------------------------------------
#pragma mark - Requesting a Password Reset
///--------------------------------------

/**
 Send a password reset request asynchronously for a specified email and sets an
 error object. If a user account exists with that email, an email will be sent to
 that address with instructions on how to reset their password.
 @param email Email of the account to send a reset password request.
 @return The task, that encapsulates the work being done.
 */
+ (BFTask<NSNumber *> *)requestPasswordResetForEmailInBackground:(NSString *)email;

/**
 Send a password reset request *asynchronously* for a specified email.
 
 If a user account exists with that email, an email will be sent to that address
 with instructions on how to reset their password.
 
 @param email Email of the account to send a reset password request.
 @param block The block to execute.
 It should have the following argument signature: `^(BOOL succeeded, NSError *error)`.
 */
+ (void)requestPasswordResetForEmailInBackground:(NSString *)email block:(nullable BEBooleanResultBlock)block;

///--------------------------------------
#pragma mark - Third-party Authentication
///--------------------------------------

/**
 Registers a third party authentication delegate.
 
 @note This method shouldn't be invoked directly unless developing a third party authentication library.
 @see BEUserAuthenticationDelegate
 
 @param delegate The third party authenticaiton delegate to be registered.
 @param authType The name of the type of third party authentication source.
 */
+ (void)registerAuthenticationDelegate:(id<BEUserAuthenticationDelegate>)delegate forAuthType:(NSString *)authType;

/**
 Logs in a user with third party authentication credentials.
 
 @note This method shouldn't be invoked directly unless developing a third party authentication library.
 @see BEUserAuthenticationDelegate
 
 @param authType The name of the type of third party authentication source.
 @param authData The user credentials of the third party authentication source.
 
 @return A `BFTask` that is resolved to `BEUser` when logging in completes.
 */
+ (BFTask<__kindof BEUser *> *)logInWithAuthTypeInBackground:(NSString *)authType
                                                    authData:(NSDictionary<NSString *, NSString *> *)authData;

/**
 Links this user to a third party authentication library.
 
 @note This method shouldn't be invoked directly unless developing a third party authentication library.
 @see BEUserAuthenticationDelegate
 
 @param authType The name of the type of third party authentication source.
 @param authData The user credentials of the third party authentication source.
 
 @return A `BFTask` that is resolved to `@YES` if linking succeeds.
 */
- (BFTask<NSNumber *> *)linkWithAuthTypeInBackground:(NSString *)authType
                                            authData:(NSDictionary<NSString *, NSString *> *)authData;

/**
 Unlinks this user from a third party authentication library.
 
 @note This method shouldn't be invoked directly unless developing a third party authentication library.
 @see BEUserAuthenticationDelegate
 
 @param authType The name of the type of third party authentication source.
 
 @return A `BFTask` that is resolved to `@YES` if unlinking succeeds.
 */
- (BFTask<NSNumber *> *)unlinkWithAuthTypeInBackground:(NSString *)authType;

/**
 Indicates whether this user is linked with a third party authentication library of a specific type.
 
 @note This method shouldn't be invoked directly unless developing a third party authentication library.
 @see BEUserAuthenticationDelegate
 
 @param authType The name of the type of third party authentication source.
 
 @return `YES` if the user is linked with a provider, otherwise `NO`.
 */
- (BOOL)isLinkedWithAuthType:(NSString *)authType;

@end
