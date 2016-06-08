//
//  CSBM.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "CSBM.h"
#import "BFTask+Private.h"
#import "CSBMInternal.h"
#import "BEManager.h"
#import "BEClientConfiguration_Private.h"
#import "BEObject+Subclass.h"
#import "BEUserPrivate.h"
#import "BELogger.h"
#import "BESession.h"
#import "BEApplication.h"
#import "BELogging.h"
#import "BEObjectSubclassingController.h"
#import "CSBM_Private.h"

@implementation CSBM
static BEManager *currentParseManager_;
static BEClientConfiguration *currentParseConfiguration_;

+ (void)initialize {
  if (self == [CSBM class]) {
    // Load all private categories, that we have...
    // Without this call - private categories - will require `-ObjC` in linker flags.
    // By explicitly calling empty method - we can avoid that.
    //[BECategoryLoader loadPrivateCategories];
    
    currentParseConfiguration_ = [BEClientConfiguration emptyConfiguration];
  }
}

///--------------------------------------
#pragma mark - Connect
///--------------------------------------

+ (void)setApplicationId:(NSString *)applicationId clientKey:(NSString *)clientKey {
  BEParameterAssert(clientKey.length, @"`clientKey` should not be nil.");
  currentParseConfiguration_.applicationId = applicationId;
  currentParseConfiguration_.clientKey = clientKey;
  currentParseConfiguration_.server = [BEInternalUtils csbmServerURLString]; // TODO: (nlutsenko) Clean this up after tests are updated.
  
  [self initializeWithConfiguration:currentParseConfiguration_];
  
  // This is needed to reset LDS's state in between initializations of Parse. We rely on this in the
  // context of unit tests.
  currentParseConfiguration_.localDatastoreEnabled = NO;
}

+ (void)initializeWithConfiguration:(BEClientConfiguration *)configuration {
  BEConsistencyAssert(configuration.applicationId.length != 0,
                      @"You must set your configuration's `applicationId` before calling %s!", __PRETTY_FUNCTION__);
//  BEConsistencyAssert(![BEApplication currentApplication].extensionEnvironment ||
//                      configuration.applicationGroupIdentifier == nil ||
//                      configuration.containingApplicationBundleIdentifier != nil,
//                      @"'containingApplicationBundleIdentifier' must be non-nil in extension environment");
//  BEConsistencyAssert(![self currentConfiguration], @"Parse is already initialized.");
  
  BEManager *manager = [[BEManager alloc] initWithConfiguration:configuration];
  [manager startManaging];
  
  currentParseManager_ = manager;
  
  BEObjectSubclassingController *subclassingController = [BEObjectSubclassingController defaultController];
  // Register built-in subclasses of BEObject so they get used.
  // We're forced to register subclasses directly this way, in order to prevent a deadlock.
  // If we ever switch to bundle scanning, this code can go away.
  [subclassingController registerSubclass:[BEUser class]];
  [subclassingController registerSubclass:[BESession class]];
//  [subclassingController registerSubclass:[BERole class]];
//  [subclassingController registerSubclass:[BEPin class]];
//  [subclassingController registerSubclass:[BEEventuallyPin class]];
#if !TARGET_OS_WATCH && !TARGET_OS_TV
  [subclassingController registerSubclass:[BEInstallation class]];
#endif
#if TARGET_OS_IOS || TARGET_OS_TV
  //[subclassingController registerSubclass:[BEProduct class]];
#endif
  
#if TARGET_OS_IOS
  //[BENetworkActivityIndicatorManager sharedManager].enabled = YES;
#endif
  
  [currentParseManager_ preloadDiskObjectsToMemoryAsync];
  
  [[self csbmModulesCollection] csbmDidInitializeWithApplicationId:configuration.applicationId clientKey:configuration.clientKey];
}

+ (nullable BEClientConfiguration *)currentConfiguration {
  return currentParseManager_.configuration;
}

+ (NSString *)getApplicationId {
  BEConsistencyAssert(currentParseManager_,
                      @"You have to call setApplicationId:clientKey: on Parse to configure Parse.");
  return currentParseManager_.configuration.applicationId;
}

+ (nullable NSString *)getClientKey {
  BEConsistencyAssert(currentParseManager_,
                      @"You have to call setApplicationId:clientKey: on Parse to configure Parse.");
  return currentParseManager_.configuration.clientKey;
}

///--------------------------------------
#pragma mark - Extensions Data Sharing
///--------------------------------------
//
//+ (void)enableDataSharingWithApplicationGroupIdentifier:(NSString *)groupIdentifier {
//  BEConsistencyAssert(!currentParseManager_,
//                      @"'enableDataSharingWithApplicationGroupIdentifier:' must be called before 'setApplicationId:clientKey'");
//  BEParameterAssert([groupIdentifier length], @"'groupIdentifier' should not be nil.");
//  BEConsistencyAssert(![BEApplication currentApplication].extensionEnvironment, @"This method cannot be used in application extensions.");
//  
//  currentParseConfiguration_.applicationGroupIdentifier = groupIdentifier;
//}
//
//+ (void)enableDataSharingWithApplicationGroupIdentifier:(NSString *)groupIdentifier
//                                  containingApplication:(NSString *)bundleIdentifier {
//  BEConsistencyAssert(!currentParseManager_,
//                      @"'enableDataSharingWithApplicationGroupIdentifier:containingApplication:' must be called before 'setApplicationId:clientKey'");
//  BEParameterAssert([groupIdentifier length], @"'groupIdentifier' should not be nil.");
//  BEParameterAssert([bundleIdentifier length], @"Containing application bundle identifier should not be nil.");
//  
//  currentParseConfiguration_.applicationGroupIdentifier = groupIdentifier;
//  currentParseConfiguration_.containingApplicationBundleIdentifier = bundleIdentifier;
//}
//
//+ (NSString *)applicationGroupIdentifierForDataSharing {
//  ParseClientConfiguration *config = currentParseManager_ ? currentParseManager_.configuration
//  : currentParseConfiguration_;
//  return config.applicationGroupIdentifier;
//}
//
//+ (NSString *)containingApplicationBundleIdentifierForDataSharing {
//  ParseClientConfiguration *config = currentParseManager_ ? currentParseManager_.configuration
//  : currentParseConfiguration_;
//  return config.containingApplicationBundleIdentifier;
//}

+ (void)_resetDataSharingIdentifiers {
  [currentParseConfiguration_ _resetDataSharingIdentifiers];
}

///--------------------------------------
#pragma mark - Local Datastore
///--------------------------------------

//+ (void)enableLocalDatastore {
//  BEConsistencyAssert(!currentParseManager_,
//                      @"'enableLocalDataStore' must be called before 'setApplicationId:clientKey:'");
//  
//  // Lazily enableLocalDatastore after init. We can't use ParseModule because
//  // ParseModule isn't processed in main thread and may cause race condition.
//  currentParseConfiguration_.localDatastoreEnabled = YES;
//}
//
//+ (BOOL)isLocalDatastoreEnabled {
//  if (!currentParseManager_) {
//    return currentParseConfiguration_.localDatastoreEnabled;
//  }
//  return currentParseManager_.offlineStoreLoaded;
//}

///--------------------------------------
#pragma mark - User Interface
///--------------------------------------

#if TARGET_OS_IOS

+ (void)offlineMessagesEnabled:(BOOL)enabled {
  // Deprecated method - shouldn't do anything.
}

+ (void)errorMessagesEnabled:(BOOL)enabled {
  // Deprecated method - shouldn't do anything.
}

#endif

///--------------------------------------
#pragma mark - Logging
///--------------------------------------

+ (void)setLogLevel:(BELogLevel)logLevel {
  [BELogger sharedLogger].logLevel = logLevel;
}

+ (BELogLevel)logLevel {
  return [BELogger sharedLogger].logLevel;
}

///--------------------------------------
#pragma mark - Private
///--------------------------------------

+ (BEManager *)_currentManager {
  return currentParseManager_;
}

+ (void)_clearCurrentManager {
  currentParseManager_ = nil;
}

///--------------------------------------
#pragma mark - Modules
///--------------------------------------

//+ (void)enableParseModule:(id<BEModule>)module {
//  [[self parseModulesCollection] addParseModule:module];
//}
//
//+ (void)disableParseModule:(id<BEModule>)module {
//  [[self parseModulesCollection] removeParseModule:module];
//}
//
//+ (BOOL)isModuleEnabled:(id<BEModule>)module {
//  return [[self parseModulesCollection] containsModule:module];
//}
//
+ (CSBMModuleCollection *)csbmModulesCollection {
  static CSBMModuleCollection *collection;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    collection = [[CSBMModuleCollection alloc] init];
  });
  return collection;
}

@end
