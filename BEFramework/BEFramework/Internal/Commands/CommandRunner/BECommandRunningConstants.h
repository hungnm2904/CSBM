//
//  BECommandRunningConstants.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

///--------------------------------------
#pragma mark - Running
///--------------------------------------

extern uint8_t const BECommandRunningDefaultMaxAttemptsCount;

///--------------------------------------
#pragma mark - Headers
///--------------------------------------

extern NSString *const BECommandHeaderNameApplicationId;
extern NSString *const BECommandHeaderNameClientKey;
extern NSString *const BECommandHeaderNameClientVersion;
extern NSString *const BECommandHeaderNameInstallationId;
extern NSString *const BECommandHeaderNameAppBuildVersion;
extern NSString *const BECommandHeaderNameAppDisplayVersion;
extern NSString *const BECommandHeaderNameOSVersion;
extern NSString *const BECommandHeaderNameSessionToken;

///--------------------------------------
#pragma mark - HTTP Method Override
///--------------------------------------

extern NSString *const BECommandParameterNameMethodOverride;
