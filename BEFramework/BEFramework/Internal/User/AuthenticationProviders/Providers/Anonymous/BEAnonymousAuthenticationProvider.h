//
//  BEAnonymousAuthenticationProvider.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEUserAuthenticationDelegate.h"

extern NSString *const BEAnonymousUserAuthenticationType;

@interface BEAnonymousAuthenticationProvider : NSObject <BEUserAuthenticationDelegate>

@property (nonatomic, copy, readonly) NSDictionary *authData;

@end
