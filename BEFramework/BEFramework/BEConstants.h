//
//  BEConstants.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

///--------------------------------------
#pragma mark - SDK Version
///--------------------------------------

#define CSBM_VERSION @"1.0.0"

///--------------------------------------
#pragma mark - Platform
///--------------------------------------

extern NSString *const _Nonnull kBEDeviceType;

///--------------------------------------
#pragma mark - Cache Policies
///--------------------------------------

/**
 `BECachePolicy` specifies different caching policies that could be used with `BEQuery`.
 
 This lets you show data when the user's device is offline,
 or when the app has just started and network requests have not yet had time to complete.
 Parse takes care of automatically flushing the cache when it takes up too much space.
 
 @warning Cache policy could only be set when Local Datastore is not enabled.
 
 @see BEQuery
 */
typedef NS_ENUM(uint8_t, BECachePolicy) {
  /**
   The query does not load from the cache or save results to the cache.
   This is the default cache policy.
   */
  kBECachePolicyIgnoreCache = 0,
  /**
   The query only loads from the cache, ignoring the network.
   If there are no cached results, this causes a `NSError` with `kBEErrorCacheMiss` code.
   */
  kBECachePolicyCacheOnly,
  /**
   The query does not load from the cache, but it will save results to the cache.
   */
  kBECachePolicyNetworkOnly,
  /**
   The query first tries to load from the cache, but if that fails, it loads results from the network.
   If there are no cached results, this causes a `NSError` with `kBEErrorCacheMiss` code.
   */
  kBECachePolicyCacheElseNetwork,
  /**
   The query first tries to load from the network, but if that fails, it loads results from the cache.
   If there are no cached results, this causes a `NSError` with `kBEErrorCacheMiss` code.
   */
  kBECachePolicyNetworkElseCache,
  /**
   The query first loads from the cache, then loads from the network.
   The callback will be called twice - first with the cached results, then with the network results.
   Since it returns two results at different times, this cache policy cannot be used with synchronous or task methods.
   */
  kBECachePolicyCacheThenNetwork
};

///--------------------------------------
#pragma mark - Logging Levels
///--------------------------------------

/**
 `BELogLevel` enum specifies different levels of logging that could be used to limit or display more messages in logs.
 
 @see `Parse.+setLogLevel:`
 @see `Parse.+logLevel`
 */
typedef NS_ENUM(uint8_t, BELogLevel) {
  /**
   Log level that disables all logging.
   */
  BELogLevelNone = 0,
  /**
   Log level that if set is going to output error messages to the log.
   */
  BELogLevelError = 1,
  /**
   Log level that if set is going to output the following messages to log:
   - Errors
   - Warnings
   */
  BELogLevelWarning = 2,
  /**
   Log level that if set is going to output the following messages to log:
   - Errors
   - Warnings
   - Informational messages
   */
  BELogLevelInfo = 3,
  /**
   Log level that if set is going to output the following messages to log:
   - Errors
   - Warnings
   - Informational messages
   - Debug messages
   */
  BELogLevelDebug = 4
};

///--------------------------------------
#pragma mark - Errors
///--------------------------------------

/**
 Error domain used for all `NSError`s in the SDK.
 */
extern NSString *const _Nonnull BEParseErrorDomain;

/**
 `BEErrorCode` enum contains all custom error codes that are used as `code` for `NSError` for callbacks on all classes.
 
 These codes are used when `domain` of `NSError` that you receive is set to `BEParseErrorDomain`.
 */
typedef NS_ENUM(NSInteger, BEErrorCode) {
  /**
   Internal server error. No information available.
   */
  kBEErrorInternalServer = 1,
  /**
   The connection to the Parse servers failed.
   */
  kBEErrorConnectionFailed = 100,
  /**
   Object doesn't exist, or has an incorrect password.
   */
  kBEErrorObjectNotFound = 101,
  /**
   You tried to find values matching a datatype that doesn't
   support exact database matching, like an array or a dictionary.
   */
  kBEErrorInvalidQuery = 102,
  /**
   Missing or invalid classname. Classnames are case-sensitive.
   They must start with a letter, and `a-zA-Z0-9_` are the only valid characters.
   */
  kBEErrorInvalidClassName = 103,
  /**
   Missing object id.
   */
  kBEErrorMissingObjectId = 104,
  /**
   Invalid key name. Keys are case-sensitive.
   They must start with a letter, and `a-zA-Z0-9_` are the only valid characters.
   */
  kBEErrorInvalidKeyName = 105,
  /**
   Malformed pointer. Pointers must be arrays of a classname and an object id.
   */
  kBEErrorInvalidPointer = 106,
  /**
   Malformed json object. A json dictionary is expected.
   */
  kBEErrorInvalidJSON = 107,
  /**
   Tried to access a feature only available internally.
   */
  kBEErrorCommandUnavailable = 108,
  /**
   Field set to incorrect type.
   */
  kBEErrorIncorrectType = 111,
  /**
   Invalid channel name. A channel name is either an empty string (the broadcast channel)
   or contains only `a-zA-Z0-9_` characters and starts with a letter.
   */
  kBEErrorInvalidChannelName = 112,
  /**
   Invalid device token.
   */
  kBEErrorInvalidDeviceToken = 114,
  /**
   Push is misconfigured. See details to find out how.
   */
  kBEErrorPushMisconfigured = 115,
  /**
   The object is too large.
   */
  kBEErrorObjectTooLarge = 116,
  /**
   That operation isn't allowed for clients.
   */
  kBEErrorOperationForbidden = 119,
  /**
   The results were not found in the cache.
   */
  kBEErrorCacheMiss = 120,
  /**
   Keys in `NSDictionary` values may not include `$` or `.`.
   */
  kBEErrorInvalidNestedKey = 121,
  /**
   Invalid file name.
   A file name can contain only `a-zA-Z0-9_.` characters and should be between 1 and 36 characters.
   */
  kBEErrorInvalidFileName = 122,
  /**
   Invalid ACL. An ACL with an invalid format was saved. This should not happen if you use `BEACL`.
   */
  kBEErrorInvalidACL = 123,
  /**
   The request timed out on the server. Typically this indicates the request is too expensive.
   */
  kBEErrorTimeout = 124,
  /**
   The email address was invalid.
   */
  kBEErrorInvalidEmailAddress = 125,
  /**
   A unique field was given a value that is already taken.
   */
  kBEErrorDuplicateValue = 137,
  /**
   Role's name is invalid.
   */
  kBEErrorInvalidRoleName = 139,
  /**
   Exceeded an application quota. Upgrade to resolve.
   */
  kBEErrorExceededQuota = 140,
  /**
   Cloud Code script had an error.
   */
  kBEScriptError = 141,
  /**
   Cloud Code validation failed.
   */
  kBEValidationError = 142,
  /**
   Product purchase receipt is missing.
   */
  kBEErrorReceiptMissing = 143,
  /**
   Product purchase receipt is invalid.
   */
  kBEErrorInvalidPurchaseReceipt = 144,
  /**
   Payment is disabled on this device.
   */
  kBEErrorPaymentDisabled = 145,
  /**
   The product identifier is invalid.
   */
  kBEErrorInvalidProductIdentifier = 146,
  /**
   The product is not found in the App Store.
   */
  kBEErrorProductNotFoundInAppStore = 147,
  /**
   The Apple server response is not valid.
   */
  kBEErrorInvalidServerResponse = 148,
  /**
   Product fails to download due to file system error.
   */
  kBEErrorProductDownloadFileSystemFailure = 149,
  /**
   Fail to convert data to image.
   */
  kBEErrorInvalidImageData = 150,
  /**
   Unsaved file.
   */
  kBEErrorUnsavedFile = 151,
  /**
   Fail to delete file.
   */
  kBEErrorFileDeleteFailure = 153,
  /**
   Application has exceeded its request limit.
   */
  kBEErrorRequestLimitExceeded = 155,
  /**
   Invalid event name.
   */
  kBEErrorInvalidEventName = 160,
  /**
   Username is missing or empty.
   */
  kBEErrorUsernameMissing = 200,
  /**
   Password is missing or empty.
   */
  kBEErrorUserPasswordMissing = 201,
  /**
   Username has already been taken.
   */
  kBEErrorUsernameTaken = 202,
  /**
   Email has already been taken.
   */
  kBEErrorUserEmailTaken = 203,
  /**
   The email is missing, and must be specified.
   */
  kBEErrorUserEmailMissing = 204,
  /**
   A user with the specified email was not found.
   */
  kBEErrorUserWithEmailNotFound = 205,
  /**
   The user cannot be altered by a client without the session.
   */
  kBEErrorUserCannotBeAlteredWithoutSession = 206,
  /**
   Users can only be created through sign up.
   */
  kBEErrorUserCanOnlyBeCreatedThroughSignUp = 207,
  /**
   An existing Facebook account already linked to another user.
   */
  kBEErrorFacebookAccountAlreadyLinked = 208,
  /**
   An existing account already linked to another user.
   */
  kBEErrorAccountAlreadyLinked = 208,
  /**
   Error code indicating that the current session token is invalid.
   */
  kBEErrorInvalidSessionToken = 209,
  kBEErrorUserIdMismatch = 209,
  /**
   Facebook id missing from request.
   */
  kBEErrorFacebookIdMissing = 250,
  /**
   Linked id missing from request.
   */
  kBEErrorLinkedIdMissing = 250,
  /**
   Invalid Facebook session.
   */
  kBEErrorFacebookInvalidSession = 251,
  /**
   Invalid linked session.
   */
  kBEErrorInvalidLinkedSession = 251,
};

///--------------------------------------
#pragma mark - Blocks
///--------------------------------------

@class BEObject;
@class BEUser;

typedef void (^BEBooleanResultBlock)(BOOL succeeded, NSError *_Nullable error);
typedef void (^BEIntegerResultBlock)(int number, NSError *_Nullable error);
typedef void (^BEArrayResultBlock)(NSArray *_Nullable objects, NSError *_Nullable error);
typedef void (^BEObjectResultBlock)(BEObject *_Nullable object,  NSError *_Nullable error);
typedef void (^BESetResultBlock)(NSSet *_Nullable channels, NSError *_Nullable error);
typedef void (^BEUserResultBlock)(BEUser *_Nullable user, NSError *_Nullable error);
typedef void (^BEDataResultBlock)(NSData *_Nullable data, NSError *_Nullable error);
typedef void (^BEDataStreamResultBlock)(NSInputStream *_Nullable stream, NSError *_Nullable error);
typedef void (^BEFilePathResultBlock)(NSString *_Nullable filePath, NSError *_Nullable error);
typedef void (^BEStringResultBlock)(NSString *_Nullable string, NSError *_Nullable error);
typedef void (^BEIdResultBlock)(_Nullable id object, NSError *_Nullable error);
typedef void (^BEProgressBlock)(int percentDone);

///--------------------------------------
#pragma mark - Network Notifications
///--------------------------------------

/**
 The name of the notification that is going to be sent before any URL request is sent.
 */
extern NSString *const _Nonnull BENetworkWillSendURLRequestNotification;

/**
 The name of the notification that is going to be sent after any URL response is received.
 */
extern NSString *const _Nonnull BENetworkDidReceiveURLResponseNotification;

/**
 The key of request(NSURLRequest) in the userInfo dictionary of a notification.
 @note This key is populated in userInfo, only if `BELogLevel` on `Parse` is set to `BELogLevelDebug`.
 */
extern NSString *const _Nonnull BENetworkNotificationURLRequestUserInfoKey;

/**
 The key of response(NSHTTPURLResponse) in the userInfo dictionary of a notification.
 @note This key is populated in userInfo, only if `BELogLevel` on `Parse` is set to `BELogLevelDebug`.
 */
extern NSString *const _Nonnull BENetworkNotificationURLResponseUserInfoKey;

/**
 The key of repsonse body (usually `NSString` with JSON) in the userInfo dictionary of a notification.
 @note This key is populated in userInfo, only if `BELogLevel` on `Parse` is set to `BELogLevelDebug`.
 */
extern NSString *const _Nonnull BENetworkNotificationURLResponseBodyUserInfoKey;


///--------------------------------------
#pragma mark - Deprecated Macros
///--------------------------------------

#ifndef PARSE_DEPRECATED
#  ifdef __deprecated_msg
#    define PARSE_DEPRECATED(_MSG) __deprecated_msg(_MSG)
#  else
#    ifdef __deprecated
#      define PARSE_DEPRECATED(_MSG) __attribute__((deprecated))
#    else
#      define PARSE_DEPRECATED(_MSG)
#    endif
#  endif
#endif

///--------------------------------------
#pragma mark - Extensions Macros
///--------------------------------------

#ifndef BE_EXTENSION_UNAVAILABLE
#  if PARSE_IOS_ONLY
#    ifdef NS_EXTENSION_UNAVAILABLE_IOS
#      define BE_EXTENSION_UNAVAILABLE(_msg) NS_EXTENSION_UNAVAILABLE_IOS(_msg)
#    else
#      define BE_EXTENSION_UNAVAILABLE(_msg)
#    endif
#  else
#    ifdef NS_EXTENSION_UNAVAILABLE_MAC
#      define BE_EXTENSION_UNAVAILABLE(_msg) NS_EXTENSION_UNAVAILABLE_MAC(_msg)
#    else
#      define BE_EXTENSION_UNAVAILABLE(_msg)
#    endif
#  endif
#endif

///--------------------------------------
#pragma mark - Swift Macros
///--------------------------------------

#ifndef BE_SWIFT_UNAVAILABLE
#  ifdef NS_SWIFT_UNAVAILABLE
#    define BE_SWIFT_UNAVAILABLE NS_SWIFT_UNAVAILABLE("")
#  else
#    define BE_SWIFT_UNAVAILABLE
#  endif
#endif

///--------------------------------------
#pragma mark - Platform Availability Defines
///--------------------------------------

#ifndef TARGET_OS_IOS
#  define TARGET_OS_IOS TARGET_OS_IPHONE
#endif
#ifndef TARGET_OS_WATCH
#  define TARGET_OS_WATCH 0
#endif
#ifndef TARGET_OS_TV
#  define TARGET_OS_TV 0
#endif

#ifndef BE_TARGET_OS_OSX
#  define BE_TARGET_OS_OSX (TARGET_OS_MAC && !TARGET_OS_IOS && !TARGET_OS_WATCH && !TARGET_OS_TV)
#endif

///--------------------------------------
#pragma mark - Avaiability Macros
///--------------------------------------

#ifndef BE_IOS_UNAVAILABLE
#  ifdef __IOS_UNAVILABLE
#    define BE_IOS_UNAVAILABLE __IOS_UNAVAILABLE
#  else
#    define BE_IOS_UNAVAILABLE
#  endif
#endif

#ifndef BE_IOS_UNAVAILABLE_WARNING
#  if TARGET_OS_IOS
#    define BE_IOS_UNAVAILABLE_WARNING _Pragma("GCC warning \"This file is unavailable on iOS.\"")
#  else
#    define BE_IOS_UNAVAILABLE_WARNING
#  endif
#endif

#ifndef BE_OSX_UNAVAILABLE
#  if BE_TARGET_OS_OSX
#    define BE_OSX_UNAVAILABLE __OSX_UNAVAILABLE
#  else
#    define BE_OSX_UNAVAILABLE
#  endif
#endif

#ifndef BE_OSX_UNAVAILABLE_WARNING
#  if BE_TARGET_OS_OSX
#    define BE_OSX_UNAVAILABLE_WARNING _Pragma("GCC warning \"This file is unavailable on OS X.\"")
#  else
#    define BE_OSX_UNAVAILABLE_WARNING
#  endif
#endif

#ifndef BE_WATCH_UNAVAILABLE
#  ifdef __WATCHOS_UNAVAILABLE
#    define BE_WATCH_UNAVAILABLE __WATCHOS_UNAVAILABLE
#  else
#    define BE_WATCH_UNAVAILABLE
#  endif
#endif

#ifndef BE_WATCH_UNAVAILABLE_WARNING
#  if TARGET_OS_WATCH
#    define BE_WATCH_UNAVAILABLE_WARNING _Pragma("GCC warning \"This file is unavailable on watchOS.\"")
#  else
#    define BE_WATCH_UNAVAILABLE_WARNING
#  endif
#endif

#ifndef BE_TV_UNAVAILABLE
#  ifdef __TVOS_PROHIBITED
#    define BE_TV_UNAVAILABLE __TVOS_PROHIBITED
#  else
#    define BE_TV_UNAVAILABLE
#  endif
#endif

#ifndef BE_TV_UNAVAILABLE_WARNING
#  if TARGET_OS_TV
#    define BE_TV_UNAVAILABLE_WARNING _Pragma("GCC warning \"This file is unavailable on tvOS.\"")
#  else
#    define BE_TV_UNAVAILABLE_WARNING
#  endif
#endif

