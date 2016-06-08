//
//  BEConstants.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEConstants.h"

#if TARGET_OS_IOS
NSString *const kBEDeviceType = @"ios";
//#elif BE_TARGET_OS_OSX
//NSString *const kBEDeviceType = @"osx";
//#elif TARGET_OS_TV
//NSString *const kBEDeviceType = @"appletv";
//#elif TARGET_OS_WATCH
//NSString *const kBEDeviceType = @"applewatch";
#endif

NSString *const BEParseErrorDomain = @"Parse";

///--------------------------------------
#pragma mark - Network Notifications
///--------------------------------------

NSString *const BENetworkWillSendURLRequestNotification = @"BENetworkWillSendURLRequestNotification";
NSString *const BENetworkDidReceiveURLResponseNotification = @"BENetworkDidReceiveURLResponseNotification";
NSString *const BENetworkNotificationURLRequestUserInfoKey = @"BENetworkNotificationURLRequestUserInfoKey";
NSString *const BENetworkNotificationURLResponseUserInfoKey = @"BENetworkNotificationURLResponseUserInfoKey";
NSString *const BENetworkNotificationURLResponseBodyUserInfoKey = @"BENetworkNotificationURLResponseBodyUserInfoKey";
