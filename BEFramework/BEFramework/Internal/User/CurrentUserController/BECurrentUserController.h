//
//  BECurrentUserController.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "BEConstants.h"
#import "BECurrentObjectControlling.h"
#import "BEDataProvider.h"
#import "BEMacros.h"

@class BFTask<__covariant BFGenericType>;
@class BEUser;

typedef NS_OPTIONS(NSUInteger, BECurrentUserLoadingOptions) {
  BECurrentUserLoadingOptionCreateLazyIfNotAvailable = 1 << 0,
};

@interface BECurrentUserController : NSObject <BECurrentObjectControlling>

//@property (nonatomic, weak, readonly) id<PFKeychainStoreProvider> commonDataSource;
//@property (nonatomic, weak, readonly) id<PFObjectFilePersistenceControllerProvider> coreDataSource;

@property (atomic, assign) BOOL automaticUsersEnabled;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

//- (instancetype)initWithStorageType:(PFCurrentObjectStorageType)storageType
//                   commonDataSource:(id<PFKeychainStoreProvider>)commonDataSource
//                     coreDataSource:(id<PFObjectFilePersistenceControllerProvider>)coreDataSource NS_DESIGNATED_INITIALIZER;
//+ (instancetype)controllerWithStorageType:(PFCurrentObjectStorageType)storageType
//                         commonDataSource:(id<PFKeychainStoreProvider>)commonDataSource
//                           coreDataSource:(id<PFObjectFilePersistenceControllerProvider>)coreDataSource;

///--------------------------------------
#pragma mark - User
///--------------------------------------

- (BFTask *)getCurrentUserAsyncWithOptions:(BECurrentUserLoadingOptions)options;

- (BFTask *)logOutCurrentUserAsync;

///--------------------------------------
#pragma mark - Session Token
///--------------------------------------

- (BFTask *)getCurrentUserSessionTokenAsync;

@end
