//
//  BEInstallation.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEInstallation.h"
#import "BEInstallationPrivate.h"

#import "BFTask+Private.h"
#import "BEApplication.h"
#import "BEAssert.h"
#import "BECoreManager.h"
#import "BECurrentInstallationController.h"
#import "BEInstallationConstants.h"
#import "BEInstallationController.h"
#import "BEInternalUtils.h"
#import "BEObject+Subclass.h"
#import "BEObjectEstimateData.h"
#import "BEObjectPrivate.h"
#import "BEQueryPrivate.h"
#import "CSBM_Private.h"
#import "BEErrorUtilities.h"

@implementation BEInstallation (Private)

static NSSet *protectedKeys;

+ (void)initialize {
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    protectedKeys = BE_SET(BEInstallationKeyDeviceType,
                           BEInstallationKeyInstallationId,
                           BEInstallationKeyTimeZone,
                           BEInstallationKeyLocaleIdentifier,
                           BEInstallationKeyParseVersion,
                           BEInstallationKeyAppVersion,
                           BEInstallationKeyAppName,
                           BEInstallationKeyAppIdentifier);
  });
}

// Clear device token. Used for testing.
- (void)_clearDeviceToken {
  [super removeObjectForKey:BEInstallationKeyDeviceToken];
}

- (BFTask<BEVoid> *)_validateDeleteAsync {
  return [[super _validateDeleteAsync] continueWithSuccessBlock:^id(BFTask<BEVoid> *task) {
    NSError *error = [BEErrorUtilities errorWithCode:kBEErrorCommandUnavailable
                                             message:@"Installation cannot be deleted"];
    return [BFTask taskWithError:error];
  }];
}

// Validates a class name. We override this to only allow the installation class name.
+ (void)_assertValidInstanceClassName:(NSString *)className {
  BEParameterAssert([className isEqualToString:[BEInstallation csbmClassName]],
                    @"Cannot initialize a BEInstallation with a custom class name.");
}

//- (BOOL)_isCurrentInstallation {
//  return (self == [[self class] _currentInstallationController].memoryCachedCurrentInstallation);
//}

- (void)_markAllFieldsDirty {
  @synchronized(self.lock) {
    NSDictionary *estimatedData = self._estimatedData.dictionaryRepresentation;
    [estimatedData enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
      [super setObject:obj forKey:key];
    }];
  }
}

- (NSString *)displayClassName {
  return NSStringFromClass([BEInstallation class]);
}

///--------------------------------------
#pragma mark - Command Handlers
///--------------------------------------

//- (BFTask *)handleSaveResultAsync:(NSDictionary *)result {
//  @weakify(self);
//  return [[super handleSaveResultAsync:result] continueWithBlock:^id(BFTask *task) {
//    @strongify(self);
//    BFTask *saveTask = [[[self class] _currentInstallationController] saveCurrentObjectAsync:self];
//    return [saveTask continueWithResult:task];
//  }];
//}

///--------------------------------------
#pragma mark - Current Installation Controller
///--------------------------------------

//+ (BECurrentInstallationController *)_currentInstallationController {
//  return [CSBM _currentManager].coreManager.currentInstallationController;
//}

@end

@implementation BEInstallation

@dynamic deviceType;
@dynamic installationId;
@dynamic deviceToken;
@dynamic timeZone;
@dynamic channels;
@dynamic badge;

///--------------------------------------
#pragma mark - BESubclassing
///--------------------------------------

+ (NSString *)csbmClassName {
  return @"_Installation";
}

+ (BEQuery *)query {
  return [super query];
}

///--------------------------------------
#pragma mark - Current Installation
///--------------------------------------

+ (instancetype)currentInstallation {
  return [[self getCurrentInstallationInBackground] waitForResult:nil withMainThreadWarning:NO];
}

//+ (BFTask<__kindof BEInstallation *> *)getCurrentInstallationInBackground {
//  return [[self _currentInstallationController] getCurrentObjectAsync];
//}

///--------------------------------------
#pragma mark - Accessors
///--------------------------------------

//- (id)objectForKey:(NSString *)key {
//  if ([key isEqualToString:BEInstallationKeyBadge] && [self _isCurrentInstallation]) {
//    // Update the data dictionary badge value from the device.
//    //[self _updateBadgeFromDevice];
//  }
//  
//  return [super objectForKey:key];
//}

- (void)setObject:(id)object forKey:(NSString *)key {
  BEParameterAssert(![protectedKeys containsObject:key],
                    @"Can't change the '%@' field of a BEInstallation.", key);
  
  if ([key isEqualToString:BEInstallationKeyBadge]) {
    // Set the application badge and update the badge value in the data dictionary.
    NSInteger badge = [object integerValue];
    BEParameterAssert(badge >= 0, @"Can't set the badge to less than zero.");
    
    [BEApplication currentApplication].iconBadgeNumber = badge;
    [super setObject:@(badge) forKey:BEInstallationKeyBadge];
  }
  
  [super setObject:object forKey:key];
}

- (void)incrementKey:(NSString *)key byAmount:(NSNumber *)amount {
  BEParameterAssert(![key isEqualToString:BEInstallationKeyBadge],
                    @"Can't atomically increment the 'badge' field of a BEInstallation.");
  
  [super incrementKey:key byAmount:amount];
}

- (void)removeObjectForKey:(NSString *)key {
  BEParameterAssert(![protectedKeys containsObject:key],
                    @"Can't remove the '%@' field of a BEInstallation.", key);
  BEParameterAssert(![key isEqualToString:BEInstallationKeyBadge],
                    @"Can't remove the 'badge' field of a BEInstallation.");
  [super removeObjectForKey:key];
}

// Internal mutators override the dynamic accessor and use super to avoid
// read-only checks on automatic fields.
- (void)setDeviceType:(NSString *)deviceType {
  [self _setObject:deviceType forKey:BEInstallationKeyDeviceType onlyIfDifferent:YES];
}

- (void)setInstallationId:(NSString *)installationId {
  [self _setObject:installationId forKey:BEInstallationKeyInstallationId onlyIfDifferent:YES];
}

- (void)setDeviceToken:(NSString *)deviceToken {
  [self _setObject:deviceToken forKey:BEInstallationKeyDeviceToken onlyIfDifferent:YES];
}

//- (void)setDeviceTokenFromData:(NSData *)deviceTokenData {
//  [self _setObject:[[BEPush pushInternalUtilClass] convertDeviceTokenToString:deviceTokenData]
//            forKey:BEInstallationKeyDeviceToken
//   onlyIfDifferent:YES];
//}

- (void)setTimeZone:(NSString *)timeZone {
  [self _setObject:timeZone forKey:BEInstallationKeyTimeZone onlyIfDifferent:YES];
}

- (void)setLocaleIdentifier:(NSString *)localeIdentifier {
  [self _setObject:localeIdentifier
            forKey:BEInstallationKeyLocaleIdentifier
   onlyIfDifferent:YES];
}

- (void)setChannels:(NSArray<NSString *> *)channels {
  [self _setObject:channels forKey:BEInstallationKeyChannels onlyIfDifferent:YES];
}

///--------------------------------------
#pragma mark - BEObject
///--------------------------------------

//- (BFTask *)saveAsync:(BFTask *)toAwait {
//  return [[super saveAsync:toAwait] continueWithBlock:^id(BFTask *task) {
//    // Do not attempt to resave an object if LDS is enabled, since changing objectId is not allowed.
//    if ([CSBM _currentManager].offlineStoreLoaded) {
//      return task;
//    }
//    
//    if (task.error.code == kBEErrorObjectNotFound) {
//      @synchronized (self.lock) {
//        // Retry the fetch as a save operation because this Installation was deleted on the server.
//        // We always want [currentInstallation save] to succeed.
//        self.objectId = nil;
//        [self _markAllFieldsDirty];
//        return [super saveAsync:nil];
//      }
//    }
//    return task;
//  }];
//}

- (BOOL)needsDefaultACL {
  return NO;
}

///--------------------------------------
#pragma mark - Automatic Info
///--------------------------------------

//- (void)_objectWillSave {
//  if ([self _isCurrentInstallation]) {
//    @synchronized(self.lock) {
//      [self _updateTimeZoneFromDevice];
//      //[self _updateBadgeFromDevice];
//      [self _updateVersionInfoFromDevice];
//      [self _updateLocaleIdentifierFromDevice];
//    }
//  }
//}

- (void)_updateTimeZoneFromDevice {
  // Get the system time zone (after clearing the cached value) and update
  // the installation if necessary.
  NSString *systemTimeZoneName = [BEInternalUtils currentSystemTimeZoneName];
  if (![systemTimeZoneName isEqualToString:self.timeZone]) {
    self.timeZone = systemTimeZoneName;
  }
}
//
//- (void)_updateBadgeFromDevice {
//  // Get the application icon and update the installation if necessary.
//  NSNumber *applicationBadge = @([BEApplication currentApplication].iconBadgeNumber);
//  NSNumber *installationBadge = [super objectForKey:BEInstallationKeyBadge];
//  if (installationBadge == nil || ![applicationBadge isEqualToNumber:installationBadge]) {
//    [super setObject:applicationBadge forKey:BEInstallationKeyBadge];
//  }
//}

- (void)_updateVersionInfoFromDevice {
  NSDictionary *appInfo = [NSBundle mainBundle].infoDictionary;
  NSString *appName = appInfo[(__bridge NSString *)kCFBundleNameKey];
  NSString *appVersion = appInfo[(__bridge NSString *)kCFBundleVersionKey];
  NSString *appIdentifier = appInfo[(__bridge NSString *)kCFBundleIdentifierKey];
  // It's possible that the app was created without an info.plist and we just
  // cannot get the data we need.
  // Note: it's important to make the possibly nil string the message receptor for
  // nil propegation instead of a BAD_ACCESS
  if (appName && ![self[BEInstallationKeyAppName] isEqualToString:appName]) {
    [super setObject:appName forKey:BEInstallationKeyAppName];
  }
  if (appVersion && ![self[BEInstallationKeyAppVersion] isEqualToString:appVersion]) {
    [super setObject:appVersion forKey:BEInstallationKeyAppVersion];
  }
  if (appIdentifier && ![self[BEInstallationKeyAppIdentifier] isEqualToString:appIdentifier]) {
    [super setObject:appIdentifier forKey:BEInstallationKeyAppIdentifier];
  }
  if (![self[BEInstallationKeyParseVersion] isEqualToString:CSBM_VERSION]) {
    [super setObject:CSBM_VERSION forKey:BEInstallationKeyParseVersion];
  }
}

/**
 Save localeIdentifier in the following format: [language code]-[COUNTRY CODE].
 
 The language codes are two-letter lowercase ISO language codes (such as "en") as defined by
 <a href="http://en.wikipedia.org/wiki/ISO_639-1">ISO 639-1</a>.
 The country codes are two-letter uppercase ISO country codes (such as "US") as defined by
 <a href="http://en.wikipedia.org/wiki/ISO_3166-1_alpha-3">ISO 3166-1</a>.
 
 Many iOS locale identifiers don't contain the country code -> inconsistencies with Android/Windows Phone.
 */
- (void)_updateLocaleIdentifierFromDevice {
  NSLocale *currentLocale = [NSLocale currentLocale];
  NSString *language = [currentLocale objectForKey:NSLocaleLanguageCode];
  NSString *countryCode = [currentLocale objectForKey:NSLocaleCountryCode];
  
  if (language.length == 0) {
    return;
  }
  
  NSString *localeIdentifier = nil;
  if (countryCode.length > 0) {
    localeIdentifier = [NSString stringWithFormat:@"%@-%@", language, countryCode];
  } else {
    localeIdentifier = language;
  }
  
  NSString *currentLocaleIdentifier = self[BEInstallationKeyLocaleIdentifier];
  if (localeIdentifier.length > 0 && ![localeIdentifier isEqualToString:currentLocaleIdentifier]) {
    // Call into super to avoid checking on protected keys.
    [super setObject:localeIdentifier forKey:BEInstallationKeyLocaleIdentifier];
  }
}

///--------------------------------------
#pragma mark - Data Source
///--------------------------------------

//+ (id<BEObjectControlling>)objectController {
//  return [CSBM _currentManager].coreManager.installationController;
//}

@end
