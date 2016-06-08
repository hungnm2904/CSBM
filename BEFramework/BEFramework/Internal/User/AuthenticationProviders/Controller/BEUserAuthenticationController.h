//
//  BEUserAuthenticationController.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEConstants.h"
#import "BEUserAuthenticationDelegate.h"
#import "BECoreDataProvider.h"

@class BFTask<__covariant BFGenericType>;
@class BEUser;

@interface BEUserAuthenticationController : NSObject

@property (nonatomic, weak, readonly) id<BECurrentUserControllerProvider, BEUserControllerProvider> dataSource;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

+ (instancetype)controllerWithDataSource:(id<BECurrentUserControllerProvider, BEUserControllerProvider>)dataSource;

///--------------------------------------
#pragma mark - Authentication Providers
///--------------------------------------

- (void)registerAuthenticationDelegate:(id<BEUserAuthenticationDelegate>)delegate forAuthType:(NSString *)authType;
- (void)unregisterAuthenticationDelegateForAuthType:(NSString *)authType;

- (id<BEUserAuthenticationDelegate>)authenticationDelegateForAuthType:(NSString *)authType;

///--------------------------------------
#pragma mark - Authentication
///--------------------------------------

- (BFTask<NSNumber *> *)restoreAuthenticationAsyncWithAuthData:(nullable NSDictionary<NSString *, NSString *> *)authData
                                                   forAuthType:(NSString *)authType;
- (BFTask<NSNumber *> *)deauthenticateAsyncWithAuthType:(NSString *)authType;

///--------------------------------------
#pragma mark - Log In
///--------------------------------------

- (BFTask<BEUser *> *)logInUserAsyncWithAuthType:(NSString *)authType
                                        authData:(NSDictionary<NSString *, NSString *> *)authData;

@end
