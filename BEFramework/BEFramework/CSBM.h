//
//  CSBM.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "BEClientConfiguration.h"
#import "BEAnonymousUtils.h"
#import "BEConfig.h"
#import "BEConstants.h"
#import "BEObject.h"
#import "BESession.h"
#import "BEUser.h"
#import "BEUserAuthenticationDelegate.h"

#import "BEInstallation.h"

@interface CSBM : NSObject

///--------------------------------------
#pragma mark - Connecting to Parse
///--------------------------------------

/**
 Sets the applicationId and clientKey of your application.
 
 @param applicationId The application id of :your Parse application.
 @param clientKey The client key of your Parse application.
 */
+ (void)setApplicationId:(NSString *)applicationId clientKey:(NSString *)clientKey;

/**
 Sets the configuration to be used for the Parse SDK.
 
 @note Re-setting the configuration after having previously sent requests through the SDK results in undefined behavior.
 
 @param configuration The new configuration to set for the SDK.
 */
+ (void)initializeWithConfiguration:(BEClientConfiguration *)configuration;

/**
 Gets the current configuration in use by the Parse SDK.
 
 @return The current configuration in use by the SDK. Returns nil if the SDK has not been initialized yet.
 */
+ (nullable BEClientConfiguration *)currentConfiguration;

/**
 The current application id that was used to configure Parse framework.
 */
+ (NSString *)getApplicationId;

/**
 The current client key that was used to configure Parse framework.
 */
+ (nullable NSString *)getClientKey;

///--------------------------------------
#pragma mark - Enabling Local Datastore
///--------------------------------------

/**
 Enable pinning in your application. This must be called before your application can use
 pinning. The recommended way is to call this method before `+setApplicationId:clientKey:`.
 */
+ (void)enableLocalDatastore BE_TV_UNAVAILABLE;

/**
 Flag that indicates whether Local Datastore is enabled.
 
 @return `YES` if Local Datastore is enabled, otherwise `NO`.
 */
+ (BOOL)isLocalDatastoreEnabled BE_TV_UNAVAILABLE;

///--------------------------------------
#pragma mark - Enabling Extensions Data Sharing
///--------------------------------------

/**
 Enables data sharing with an application group identifier.
 
 After enabling - Local Datastore, `BEUser.+currentUser`, `BEInstallation.+currentInstallation` and all eventually commands
 are going to be available to every application/extension in a group that have the same Parse applicationId.
 
 @warning This method is required to be called before `+setApplicationId:clientKey:`.
 
 @param groupIdentifier Application Group Identifier to share data with.
 */
+ (void)enableDataSharingWithApplicationGroupIdentifier:(NSString *)groupIdentifier BE_EXTENSION_UNAVAILABLE("Use `enableDataSharingWithApplicationGroupIdentifier:containingApplication:`.") BE_WATCH_UNAVAILABLE BE_TV_UNAVAILABLE;

/**
 Enables data sharing with an application group identifier.
 
 After enabling - Local Datastore, `BEUser.+currentUser`, `BEInstallation.+currentInstallation` and all eventually commands
 are going to be available to every application/extension in a group that have the same Parse applicationId.
 
 @warning This method is required to be called before `+setApplicationId:clientKey:`.
 This method can only be used by application extensions.
 
 @param groupIdentifier Application Group Identifier to share data with.
 @param bundleIdentifier Bundle identifier of the containing application.
 */
+ (void)enableDataSharingWithApplicationGroupIdentifier:(NSString *)groupIdentifier
                                  containingApplication:(NSString *)bundleIdentifier BE_WATCH_UNAVAILABLE BE_TV_UNAVAILABLE;

/**
 Application Group Identifier for Data Sharing.
 
 @return `NSString` value if data sharing is enabled, otherwise `nil`.
 */
+ (NSString *)applicationGroupIdentifierForDataSharing BE_WATCH_UNAVAILABLE BE_TV_UNAVAILABLE;

/**
 Containing application bundle identifier for Data Sharing.
 
 @return `NSString` value if data sharing is enabled, otherwise `nil`.
 */
+ (NSString *)containingApplicationBundleIdentifierForDataSharing BE_WATCH_UNAVAILABLE BE_TV_UNAVAILABLE;

#if TARGET_OS_IOS

///--------------------------------------
#pragma mark - Configuring UI Settings
///--------------------------------------

/**
 Set whether to show offline messages when using a Parse view or view controller related classes.
 
 @param enabled Whether a `UIAlertView` should be shown when the device is offline
 and network access is required from a view or view controller.
 
 @deprecated This method has no effect.
 */
+ (void)offlineMessagesEnabled:(BOOL)enabled PARSE_DEPRECATED("This method is deprecated and has no effect.");

/**
 Set whether to show an error message when using a Parse view or view controller related classes
 and a Parse error was generated via a query.
 
 @param enabled Whether a `UIAlertView` should be shown when an error occurs.
 
 @deprecated This method has no effect.
 */
+ (void)errorMessagesEnabled:(BOOL)enabled PARSE_DEPRECATED("This method is deprecated and has no effect.");

#endif

///--------------------------------------
#pragma mark - Logging
///--------------------------------------

/**
 Sets the level of logging to display.
 
 By default:
 - If running inside an app that was downloaded from iOS App Store - it is set to `BELogLevelNone`
 - All other cases - it is set to `BELogLevelWarning`
 
 @param logLevel Log level to set.
 @see BELogLevel
 */
+ (void)setLogLevel:(BELogLevel)logLevel;

/**
 Log level that will be displayed.
 
 By default:
 
 - If running inside an app that was downloaded from iOS App Store - it is set to `BELogLevelNone`
 - All other cases - it is set to `BELogLevelWarning`
 
 @return A `BELogLevel` value.
 @see BELogLevel
 */
+ (BELogLevel)logLevel;

@end
