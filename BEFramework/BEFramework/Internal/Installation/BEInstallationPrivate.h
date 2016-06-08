//
//  BEInstallationPrivate.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEInstallation.h"

@interface BEInstallation (Private)

- (void)_clearDeviceToken;
- (void)_markAllFieldsDirty;

@end

@interface BEInstallation ()

// Private read-write declarations of publicly-readonly fields.
@property (nonatomic, copy, readwrite) NSString *deviceType;
@property (nonatomic, copy, readwrite) NSString *installationId;
@property (nonatomic, copy, readwrite) NSString *timeZone;

@end