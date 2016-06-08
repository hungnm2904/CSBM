//
//  BEApplication.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEConstants.h"

#if TARGET_OS_IOS || TARGET_OS_TV
#import <UIKit/UIKit.h>
#elif TARGET_OS_WATCH
@class UIApplication;
#elif PF_TARGET_OS_OSX
#import <AppKit/AppKit.h>
@compatibility_alias UIApplication NSApplication;
#endif

@interface BEApplication : NSObject

@property (nonatomic, strong, readonly) UIApplication *systemApplication;

@property (nonatomic, assign, readonly, getter=isAppStoreEnvironment) BOOL appStoreEnvironment;
@property (nonatomic, assign, readonly, getter=isExtensionEnvironment) BOOL extensionEnvironment;

@property (nonatomic, assign) NSInteger iconBadgeNumber;

+ (instancetype)currentApplication;

@end
