//
//  BERESTUserCommand.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BERESTUserCommand.h"

#import "BEAssert.h"
#import "BEHTTPRequest.h"

static NSString *const BERESTUserCommandRevocableSessionHeader = @"X-Parse-Revocable-Session";
static NSString *const BERESTUserCommandRevocableSessionHeaderEnabledValue = @"1";

@interface BERESTUserCommand ()

@property (nonatomic, assign, readwrite) BOOL revocableSessionEnabled;

@end

@implementation BERESTUserCommand

///--------------------------------------
#pragma mark - Init
///--------------------------------------

+ (instancetype)_commandWithHTTPPath:(NSString *)path
                          httpMethod:(NSString *)httpMethod
                          parameters:(NSDictionary *)parameters
                        sessionToken:(NSString *)sessionToken
                    revocableSession:(BOOL)revocableSessionEnabled {
  BERESTUserCommand *command = [self commandWithHTTPPath:path
                                              httpMethod:httpMethod
                                              parameters:parameters
                                            sessionToken:sessionToken];
  if (revocableSessionEnabled) {
    command.additionalRequestHeaders = @{ BERESTUserCommandRevocableSessionHeader :
                                            BERESTUserCommandRevocableSessionHeaderEnabledValue};
  }
  command.revocableSessionEnabled = revocableSessionEnabled;
  return command;
}

///--------------------------------------
#pragma mark - Log In
///--------------------------------------

+ (instancetype)logInUserCommandWithUsername:(NSString *)username
                                    password:(NSString *)password
                            revocableSession:(BOOL)revocableSessionEnabled {
  NSDictionary *parameters = @{ @"username" : username,
                                @"password" : password };
  return [self _commandWithHTTPPath:@"login"
                         httpMethod:BEHTTPRequestMethodGET
                         parameters:parameters
                       sessionToken:nil
                   revocableSession:revocableSessionEnabled];
}

+ (instancetype)serviceLoginUserCommandWithAuthenticationType:(NSString *)authenticationType
                                           authenticationData:(NSDictionary *)authenticationData
                                             revocableSession:(BOOL)revocableSessionEnabled {
  NSDictionary *parameters = @{ @"authData" : @{ authenticationType : authenticationData } };
  return [self serviceLoginUserCommandWithParameters:parameters
                                    revocableSession:revocableSessionEnabled
                                        sessionToken:nil];
}

+ (instancetype)serviceLoginUserCommandWithParameters:(NSDictionary *)parameters
                                     revocableSession:(BOOL)revocableSessionEnabled
                                         sessionToken:(NSString *)sessionToken {
  return [self _commandWithHTTPPath:@"users"
                         httpMethod:BEHTTPRequestMethodPOST
                         parameters:parameters
                       sessionToken:sessionToken
                   revocableSession:revocableSessionEnabled];
}

///--------------------------------------
#pragma mark - Sign Up
///--------------------------------------

+ (instancetype)signUpUserCommandWithParameters:(NSDictionary *)parameters
                               revocableSession:(BOOL)revocableSessionEnabled
                                   sessionToken:(NSString *)sessionToken {
  return [self _commandWithHTTPPath:@"users"
                         httpMethod:BEHTTPRequestMethodPOST
                         parameters:parameters
                       sessionToken:sessionToken
                   revocableSession:revocableSessionEnabled];
}

///--------------------------------------
#pragma mark - Current User
///--------------------------------------

+ (instancetype)getCurrentUserCommandWithSessionToken:(NSString *)sessionToken {
  return [self commandWithHTTPPath:@"users/me"
                        httpMethod:BEHTTPRequestMethodGET
                        parameters:nil
                      sessionToken:sessionToken];
}

+ (instancetype)upgradeToRevocableSessionCommandWithSessionToken:(NSString *)sessionToken {
  return [self commandWithHTTPPath:@"upgradeToRevocableSession"
                        httpMethod:BEHTTPRequestMethodPOST
                        parameters:nil
                      sessionToken:sessionToken];
}

+ (instancetype)logOutUserCommandWithSessionToken:(NSString *)sessionToken {
  return [self commandWithHTTPPath:@"logout"
                        httpMethod:BEHTTPRequestMethodPOST
                        parameters:nil
                      sessionToken:sessionToken];
}

///--------------------------------------
#pragma mark - Additional User Commands
///--------------------------------------

+ (instancetype)resetPasswordCommandForUserWithEmail:(NSString *)email {
  return [self commandWithHTTPPath:@"requestPasswordReset"
                        httpMethod:BEHTTPRequestMethodPOST
                        parameters:@{ @"email" : email }
                      sessionToken:nil];
}

@end
