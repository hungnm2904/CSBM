//
//  BEUserAuthenticationDelegate.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEConstants.h"

/**
 Provides a general interface for delegation of third party authentication with `PFUser`s.
 */
@protocol BEUserAuthenticationDelegate <NSObject>

/**
 Called when restoring third party authentication credentials that have been serialized,
 such as session keys, user id, etc.
 
 @note This method will be executed on a background thread.
 
 @param authData The auth data for the provider. This value may be `nil` when unlinking an account.
 
 @return `YES` - if the `authData` was succesfully synchronized,
 or `NO` if user should not longer be associated because of bad `authData`.
 */
- (BOOL)restoreAuthenticationWithAuthData:(nullable NSDictionary<NSString *, NSString *> *)authData;

@end