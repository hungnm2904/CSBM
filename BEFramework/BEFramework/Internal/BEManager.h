//
//  BEManager.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEClientConfiguration.h"
#import "BEConstants.h"
#import "BEMacros.h"
#import "BEDataProvider.h"
#import "BECoreManager.h"

@class BFTask<__covariant BFGenericType>;
@class BEInstallationIdentifierStore;

@interface BEManager : NSObject <BECommandRunnerProvider, BEInstallationIdentifierStoreProvider>

@property (nonatomic, copy, readonly) BEClientConfiguration *configuration;

@property (nonatomic, strong, readonly) BECoreManager *coreManager;

///--------------------------------------
#pragma mark - Initialization
///--------------------------------------

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

/**
 Initializes an instance of ParseManager class.
 
 @param configuration Configuration of parse app.
 
 @return `ParseManager` instance.
 */
- (instancetype)initWithConfiguration:(BEClientConfiguration *)configuration NS_DESIGNATED_INITIALIZER;

/**
 Begins all necessary operations for this manager to become active.
 */
- (void)startManaging;

///--------------------------------------
#pragma mark - Offline Store
///--------------------------------------

//- (void)loadOfflineStoreWithOptions:(PFOfflineStoreOptions)options;

///--------------------------------------
#pragma mark - Eventually Queue
///--------------------------------------

- (void)clearEventuallyQueue;

///--------------------------------------
#pragma mark - Core Manager
///--------------------------------------

- (void)unloadCoreManager;

///--------------------------------------
#pragma mark - Preloading
///--------------------------------------

- (BFTask *)preloadDiskObjectsToMemoryAsync;
@end
