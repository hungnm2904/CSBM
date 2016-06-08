//
//  BERESTUserCommand.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BERESTCommand.h"

@interface BERESTUserCommand : BERESTCommand

@property (nonatomic, assign, readonly) BOOL revocableSessionEnabled;

///--------------------------------------
#pragma mark - Log In
///--------------------------------------

+ (instancetype)logInUserCommandWithUsername:(NSString *)username
                                    password:(NSString *)password
                            revocableSession:(BOOL)revocableSessionEnabled;
+ (instancetype)serviceLoginUserCommandWithAuthenticationType:(NSString *)authenticationType
                                           authenticationData:(NSDictionary *)authenticationData
                                             revocableSession:(BOOL)revocableSessionEnabled;
+ (instancetype)serviceLoginUserCommandWithParameters:(NSDictionary *)parameters
                                     revocableSession:(BOOL)revocableSessionEnabled
                                         sessionToken:(nullable NSString *)sessionToken;

///--------------------------------------
#pragma mark - Sign Up
///--------------------------------------

+ (instancetype)signUpUserCommandWithParameters:(NSDictionary *)parameters
                               revocableSession:(BOOL)revocableSessionEnabled
                                   sessionToken:(nullable NSString *)sessionToken;

///--------------------------------------
#pragma mark - Current User
///--------------------------------------

+ (instancetype)getCurrentUserCommandWithSessionToken:(NSString *)sessionToken;
+ (instancetype)upgradeToRevocableSessionCommandWithSessionToken:(NSString *)sessionToken;
+ (instancetype)logOutUserCommandWithSessionToken:(NSString *)sessionToken;

///--------------------------------------
#pragma mark - Password Rest
///--------------------------------------

+ (instancetype)resetPasswordCommandForUserWithEmail:(NSString *)email;

@end
