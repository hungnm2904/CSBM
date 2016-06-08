//
//  BEAnonymousAuthenticationProvider.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEAnonymousAuthenticationProvider.h"
#import <Bolts/BFTask.h>

NSString *const BEAnonymousUserAuthenticationType = @"anonymous";

@implementation BEAnonymousAuthenticationProvider

///--------------------------------------
#pragma mark - PFAnonymousAuthenticationProvider
///--------------------------------------

- (BOOL)restoreAuthenticationWithAuthData:(NSDictionary *)authData {
  return YES;
}

///--------------------------------------
#pragma mark - Accessors
///--------------------------------------

- (NSDictionary *)authData {
  NSString *uuidString = [NSUUID UUID].UUIDString;
  uuidString = uuidString.lowercaseString;
  return @{ @"id" : uuidString };
}

@end
