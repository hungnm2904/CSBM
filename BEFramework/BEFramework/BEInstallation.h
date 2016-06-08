//
//  BEInstallation.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEObject.h"
#import "BESubclassing.h"
#import <Bolts/BFTask.h>

@interface BEInstallation : BEObject<BESubclassing>

///--------------------------------------
#pragma mark - Accessing the Current Installation
///--------------------------------------

/**
 Gets the currently-running installation from disk and returns an instance of it.
 
 If this installation is not stored on disk this method will create a new `BEInstallation`
 with `deviceType` and `installationId` fields set to those of the current installation.
 
 @result Returns a `BEInstallation` that represents the currently-running installation if it could be loaded from disk, otherwise - `nil`.
 */
+ (nullable instancetype)currentInstallation;

/**
 *Asynchronously* loads the currently-running installation from disk and returns an instance of it.
 
 If this installation is not stored on disk this method will create a new `BEInstallation`
 with `deviceType` and `installationId` fields set to those of the current installation.
 
 @result Returns a task that incapsulates the current installation.
 */
+ (BFTask<__kindof BEInstallation *> *)getCurrentInstallationInBackground;

///--------------------------------------
#pragma mark - Installation Properties
///--------------------------------------

/**
 The device type for the `BEInstallation`.
 */
@property (nonatomic, copy, readonly) NSString *deviceType;

/**
 The installationId for the `BEInstallation`.
 */
@property (nonatomic, copy, readonly) NSString *installationId;

/**
 The device token for the `BEInstallation`.
 */
@property (nullable, nonatomic, copy) NSString *deviceToken;

/**
 The badge for the `BEInstallation`.
 */
@property (nonatomic, assign) NSInteger badge;

/**
 The name of the time zone for the `BEInstallation`.
 */
@property (nullable, nonatomic, copy, readonly) NSString *timeZone;

/**
 The channels for the `BEInstallation`.
 */
@property (nullable, nonatomic, copy) NSArray<NSString *> *channels;

/**
 Sets the device token string property from an `NSData`-encoded token.
 
 @param deviceTokenData A token that identifies the device.
 */
- (void)setDeviceTokenFromData:(nullable NSData *)deviceTokenData;

///--------------------------------------
#pragma mark - Querying for Installations
///--------------------------------------

/**
 Creates a `BEQuery` for `BEInstallation` objects.
 
 Only the following types of queries are allowed for installations:
 
 - `[query getObjectWithId:<value>]`
 - `[query whereKey:@"installationId" equalTo:<value>]`
 - `[query whereKey:@"installationId" matchesKey:<key in query> inQuery:<query>]`
 
 You can add additional query conditions, but one of the above must appear as a top-level `AND` clause in the query.
 */
+ (nullable BEQuery *)query;

@end
