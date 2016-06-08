//
//  BEAnonymousUtils_Private.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "BEAnonymousUtils.h"
#import "BEAnonymousAuthenticationProvider.h"

@class PBEAnonymousAuthenticationProvider;
@class BEUser;

@interface BEAnonymousUtils (Private)

+ (BEAnonymousAuthenticationProvider *)_authenticationProvider;
+ (void)_clearAuthenticationProvider;

+ (BEUser *)_lazyLogIn;

@end
