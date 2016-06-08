//
//  BECoreManager.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "BECoreDataProvider.h"
#import "BEDataProvider.h"

@class BEInstallationIdentifierStore;

@protocol BECoreManagerDataSource<BECommandRunnerProvider, BEInstallationIdentifierStoreProvider,
BEPersistenceControllerProvider>

@property (nonatomic, strong, readonly) BEInstallationIdentifierStore *installationIdentifierStore;

@end

@class BEConfigController;
@class BEQueryController;
@class BESessionController;
@class BEObjectSubclassingController;
@class BEQueryController;
@class BESessionController;

@interface BECoreManager : NSObject<BEObjectControllerProvider, BEObjectBatchController,
BEUserAuthenticationControllerProvider, BECurrentUserControllerProvider, BEUserControllerProvider>

@property (nonatomic, weak, readonly) id<BECoreManagerDataSource> dataSource;

@property (null_resettable, nonatomic, strong) BEQueryController *queryController;
@property (null_resettable, nonatomic, strong) BEConfigController *configController;
@property (null_resettable, nonatomic, strong) BESessionController *sessionController;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

- (instancetype)initWithDataSource:(id<BECoreManagerDataSource>)dataSource NS_DESIGNATED_INITIALIZER;

+ (instancetype)managerWithDataSource:(id<BECoreManagerDataSource>)dataSource;

///--------------------------------------
#pragma mark - ObjectFilePersistenceController
///--------------------------------------

- (void)unloadObjectFilePersistenceController;

@end
