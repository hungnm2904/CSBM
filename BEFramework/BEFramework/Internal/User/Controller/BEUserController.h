//
//  BEUserController.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEDataProvider.h"
#import "BEObjectControlling.h"
#import "BECoreDataProvider.h"

@interface BEUserController : NSObject

@property (nonatomic, weak, readonly) id<BECommandRunnerProvider> commonDataSource;
@property (nonatomic, weak, readonly) id<BECurrentUserControllerProvider> coreDataSource;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init NS_UNAVAILABLE;
- (instancetype)initWithCommonDataSource:(id<BECommandRunnerProvider>)commonDataSource
                          coreDataSource:(id<BECurrentUserControllerProvider>)coreDataSource;
+ (instancetype)controllerWithCommonDataSource:(id<BECommandRunnerProvider>)commonDataSource
                                coreDataSource:(id<BECurrentUserControllerProvider>)coreDataSource;

///--------------------------------------
#pragma mark - Log In
///--------------------------------------

- (BFTask *)logInCurrentUserAsyncWithSessionToken:(NSString *)sessionToken;
- (BFTask *)logInCurrentUserAsyncWithUsername:(NSString *)username
                                     password:(NSString *)password
                             revocableSession:(BOOL)revocableSession;

//TODO: (nlutsenko) Move this method into BEUserAuthenticationController after BEUser is decoupled further.
- (BFTask *)logInCurrentUserAsyncWithAuthType:(NSString *)authType
                                     authData:(NSDictionary *)authData
                             revocableSession:(BOOL)revocableSession;

///--------------------------------------
#pragma mark - Reset Password
///--------------------------------------

- (BFTask *)requestPasswordResetAsyncForEmail:(NSString *)email;

///--------------------------------------
#pragma mark - Log Out
///--------------------------------------

- (BFTask *)logOutUserAsyncWithSessionToken:(NSString *)sessionToken;
@end
