//
//  BECurrentInstallationController.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEConstants.h"
#import "BECoreDataProvider.h"
#import "BECurrentObjectControlling.h"
#import "BEDataProvider.h"
#import "BEMacros.h"

extern NSString *const BECurrentInstallationFileName;
extern NSString *const BECurrentInstallationPinName;

@class BFTask<__covariant BFGenericType>;
@class BEInstallation;

BE_TV_UNAVAILABLE BE_WATCH_UNAVAILABLE @interface BECurrentInstallationController : NSObject <BECurrentObjectControlling>

@property (nonatomic, weak, readonly) id<BEInstallationIdentifierStoreProvider> commonDataSource;
//@property (nonatomic, weak, readonly) id<BEObjectFilePersistenceControllerProvider> coreDataSource;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init NS_UNAVAILABLE;
//- (instancetype)initWithStorageType:(BECurrentObjectStorageType)dataStorageType
//                   commonDataSource:(id<BEInstallationIdentifierStoreProvider>)commonDataSource
//                     coreDataSource:(id<BEObjectFilePersistenceControllerProvider>)coreDataSource;
//
//+ (instancetype)controllerWithStorageType:(BECurrentObjectStorageType)dataStorageType
//                         commonDataSource:(id<BEInstallationIdentifierStoreProvider>)commonDataSource
//                           coreDataSource:(id<BEObjectFilePersistenceControllerProvider>)coreDataSource;

///--------------------------------------
#pragma mark - Installation
///--------------------------------------

@property (nonatomic, strong, readonly) BEInstallation *memoryCachedCurrentInstallation;

- (BFTask *)clearCurrentInstallationAsync;
- (BFTask *)clearMemoryCachedCurrentInstallationAsync;

@end
