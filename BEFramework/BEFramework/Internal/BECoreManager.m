//
//  BECoreManager.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BECoreManager.h"
#import "BEAssert.h"
#import "BECacheQueryController.h"
#import "BEConfigController.h"
#import "BECurrentUserController.h"
#import "BEMacros.h"
#import "BERESTObjectBatchCommand.h"
#import "BEObjectController.h"
#import "BEObjectSubclassingController.h"
#import "BESessionController.h"
#import "BEUserAuthenticationController.h"
#import "BEUserController.h"
#import "BEObjectBatchController.h"

@interface BECoreManager () {
  dispatch_queue_t _controllerAccessQueue;
}

@end

@implementation BECoreManager
@synthesize queryController = _queryController;
@synthesize configController = _configController;
@synthesize objectController = _objectController;
@synthesize objectBatchController = _objectBatchController;
@synthesize userAuthenticationController = _userAuthenticationController;
@synthesize sessionController = _sessionController;
@synthesize currentUserController = _currentUserController;
@synthesize userController = _userController;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithDataSource:(id<BECoreManagerDataSource>)dataSource {
  self = [super init];
  if (!self) return nil;
  
  _dataSource = dataSource;
  
  //_locationManagerAccessQueue = dispatch_queue_create("com.parse.core.locationManager", DISPATCH_QUEUE_SERIAL);
  _controllerAccessQueue = dispatch_queue_create("com.parse.core.controller.accessQueue", DISPATCH_QUEUE_SERIAL);
  //_objectLocalIdStoreAccessQueue = dispatch_queue_create("com.parse.core.object.localIdStore", DISPATCH_QUEUE_SERIAL);
  
  return self;
}

+ (instancetype)managerWithDataSource:(id<BECoreManagerDataSource>)dataSource {
  return [[self alloc] initWithDataSource:dataSource];
}

///--------------------------------------
#pragma mark - LocationManager
///--------------------------------------

//- (BELocationManager *)locationManager {
//  __block BELocationManager *manager;
//  dispatch_sync(_locationManagerAccessQueue, ^{
//    if (!_locationManager) {
//      _locationManager = [[BELocationManager alloc] init];
//    }
//    manager = _locationManager;
//  });
//  return manager;
//}
//
/////--------------------------------------
//#pragma mark - DefaultACLController
/////--------------------------------------
//
//- (BEDefaultACLController *)defaultACLController {
//  __block BEDefaultACLController *controller = nil;
//  dispatch_sync(_controllerAccessQueue, ^{
//    if (!_defaultACLController) {
//      _defaultACLController = [BEDefaultACLController controllerWithDataSource:self];
//    }
//    controller = _defaultACLController;
//  });
//  return controller;
//}

///--------------------------------------
#pragma mark - QueryController
///--------------------------------------

//- (BEQueryController *)queryController {
//  __block BEQueryController *queryController;
//  dispatch_sync(_controllerAccessQueue, ^{
//    if (!_queryController) {
//      id<BECoreManagerDataSource> dataSource = self.dataSource;
//      if (dataSource.offlineStoreLoaded) {
//        _queryController = [BEOfflineQueryController controllerWithCommonDataSource:dataSource
//                                                                     coreDataSource:self];
//      } else {
//        _queryController = [BECachedQueryController controllerWithCommonDataSource:dataSource];
//      }
//    }
//    queryController = _queryController;
//  });
//  return queryController;
//}

- (void)setQueryController:(BEQueryController *)queryController {
  dispatch_sync(_controllerAccessQueue, ^{
    _queryController = queryController;
  });
}

///--------------------------------------
#pragma mark - FileController
///--------------------------------------

//- (BEFileController *)fileController {
//  __block BEFileController *controller = nil;
//  dispatch_sync(_controllerAccessQueue, ^{
//    if (!_fileController) {
//      _fileController = [BEFileController controllerWithDataSource:self.dataSource];
//    }
//    controller = _fileController;
//  });
//  return controller;
//}
//
//- (void)setFileController:(BEFileController *)fileController {
//  dispatch_sync(_controllerAccessQueue, ^{
//    _fileController = fileController;
//  });
//}

///--------------------------------------
#pragma mark - CloudCodeController
///--------------------------------------

//- (BECloudCodeController *)cloudCodeController {
//  __block BECloudCodeController *controller = nil;
//  dispatch_sync(_controllerAccessQueue, ^{
//    if (!_cloudCodeController) {
//      _cloudCodeController = [[BECloudCodeController alloc] initWithDataSource:self.dataSource];
//    }
//    controller = _cloudCodeController;
//  });
//  return controller;
//}
//
//- (void)setCloudCodeController:(BECloudCodeController *)cloudCodeController {
//  dispatch_sync(_controllerAccessQueue, ^{
//    _cloudCodeController = cloudCodeController;
//  });
//}

///--------------------------------------
#pragma mark - ConfigController
///--------------------------------------

- (BEConfigController *)configController {
  __block BEConfigController *controller = nil;
  dispatch_sync(_controllerAccessQueue, ^{
    if (!_configController) {
      _configController = [[BEConfigController alloc] initWithDataSource:self.dataSource];
    }
    controller = _configController;
  });
  return controller;
}

- (void)setConfigController:(BEConfigController *)configController {
  dispatch_sync(_controllerAccessQueue, ^{
    _configController = configController;
  });
}

///--------------------------------------
#pragma mark - ObjectController
///--------------------------------------

//- (BEObjectController *)objectController {
//  __block BEObjectController *controller = nil;
//  dispatch_sync(_controllerAccessQueue, ^{
//    if (!_objectController) {
//      id<BECoreManagerDataSource> dataSource = self.dataSource;
//      if (dataSource.offlineStoreLoaded) {
//        _objectController = [BEOfflineObjectController controllerWithDataSource:dataSource];
//      } else {
//        _objectController = [BEObjectController controllerWithDataSource:dataSource];
//      }
//    }
//    controller = _objectController;
//  });
//  return controller;
//}

- (void)setObjectController:(BEObjectController *)controller {
  dispatch_sync(_controllerAccessQueue, ^{
    _objectController = controller;
  });
}

///--------------------------------------
#pragma mark - ObjectBatchController
///--------------------------------------

- (BEObjectBatchController *)objectBatchController {
  __block BEObjectBatchController *controller = nil;
  dispatch_sync(_controllerAccessQueue, ^{
    if (!_objectBatchController) {
      _objectBatchController = [BEObjectBatchController controllerWithDataSource:self.dataSource];
    }
    controller = _objectBatchController;
  });
  return controller;
}

///--------------------------------------
#pragma mark - ObjectFilePersistenceController
///--------------------------------------

//- (BEObjectFilePersistenceController *)objectFilePersistenceController {
//  __block BEObjectFilePersistenceController *controller = nil;
//  dispatch_sync(_controllerAccessQueue, ^{
//    if (!_objectFilePersistenceController) {
//      _objectFilePersistenceController = [BEObjectFilePersistenceController controllerWithDataSource:self.dataSource];
//    }
//    controller = _objectFilePersistenceController;
//  });
//  return controller;
//}
//
//- (void)unloadObjectFilePersistenceController {
//  dispatch_sync(_controllerAccessQueue, ^{
//    _objectFilePersistenceController = nil;
//  });
//}

///--------------------------------------
#pragma mark - Pinning Object Store
///--------------------------------------

//- (BEPinningObjectStore *)pinningObjectStore {
//  __block BEPinningObjectStore *controller = nil;
//  dispatch_sync(_controllerAccessQueue, ^{
//    if (!_pinningObjectStore) {
//      _pinningObjectStore = [BEPinningObjectStore storeWithDataSource:self.dataSource];
//    }
//    controller = _pinningObjectStore;
//  });
//  return controller;
//}
//
//- (void)setPinningObjectStore:(BEPinningObjectStore *)pinningObjectStore {
//  dispatch_sync(_controllerAccessQueue, ^{
//    _pinningObjectStore = pinningObjectStore;
//  });
//}
//
/////--------------------------------------
//#pragma mark - Object LocalId Store
/////--------------------------------------
//
//- (BEObjectLocalIdStore *)objectLocalIdStore {
//  __block BEObjectLocalIdStore *store = nil;
//  @weakify(self);
//  dispatch_sync(_objectLocalIdStoreAccessQueue, ^{
//    @strongify(self);
//    if (!_objectLocalIdStore) {
//      _objectLocalIdStore = [[BEObjectLocalIdStore alloc] initWithDataSource:self.dataSource];
//    }
//    store = _objectLocalIdStore;
//  });
//  return store;
//}
//
//- (void)setObjectLocalIdStore:(BEObjectLocalIdStore *)objectLocalIdStore {
//  dispatch_sync(_objectLocalIdStoreAccessQueue, ^{
//    _objectLocalIdStore = objectLocalIdStore;
//  });
//}

///--------------------------------------
#pragma mark - UserAuthenticationController
///--------------------------------------

- (BEUserAuthenticationController *)userAuthenticationController {
  __block BEUserAuthenticationController *controller = nil;
  dispatch_sync(_controllerAccessQueue, ^{
    if (!_userAuthenticationController) {
      _userAuthenticationController = [BEUserAuthenticationController controllerWithDataSource:self];
    }
    controller = _userAuthenticationController;
  });
  return controller;
}

- (void)setUserAuthenticationController:(BEUserAuthenticationController *)userAuthenticationController {
  dispatch_sync(_controllerAccessQueue, ^{
    _userAuthenticationController = userAuthenticationController;
  });
}

///--------------------------------------
#pragma mark - SessionController
///--------------------------------------

- (BESessionController *)sessionController {
  __block BESessionController *controller = nil;
  dispatch_sync(_controllerAccessQueue, ^{
    if (!_sessionController) {
      _sessionController = [BESessionController controllerWithDataSource:self.dataSource];
    }
    controller = _sessionController;
  });
  return controller;
}

- (void)setSessionController:(BESessionController *)sessionController {
  dispatch_sync(_controllerAccessQueue, ^{
    _sessionController = sessionController;
  });
}

#if !TARGET_OS_WATCH && !TARGET_OS_TV

///--------------------------------------
#pragma mark - Current Installation Controller
///--------------------------------------

//- (BECurrentInstallationController *)currentInstallationController {
//  __block BECurrentInstallationController *controller = nil;
//  dispatch_sync(_controllerAccessQueue, ^{
//    if (!_currentInstallationController) {
//      id<BECoreManagerDataSource> dataSource = self.dataSource;
//      BECurrentObjectStorageType storageType = (dataSource.offlineStore ?
//                                                BECurrentObjectStorageTypeOfflineStore :
//                                                BECurrentObjectStorageTypeFile);
//      _currentInstallationController = [BECurrentInstallationController controllerWithStorageType:storageType
//                                                                                 commonDataSource:dataSource
//                                                                                   coreDataSource:self];
//    }
//    controller = _currentInstallationController;
//  });
//  return controller;
//}
//
//- (void)setCurrentInstallationController:(BECurrentInstallationController *)controller {
//  dispatch_sync(_controllerAccessQueue, ^{
//    _currentInstallationController = controller;
//  });
//}

#endif

///--------------------------------------
#pragma mark - Current User Controller
///--------------------------------------

//- (BECurrentUserController *)currentUserController {
//  __block BECurrentUserController *controller = nil;
//  dispatch_sync(_controllerAccessQueue, ^{
//    if (!_currentUserController) {
//      id<BECoreManagerDataSource> dataSource = self.dataSource;
//      BECurrentObjectStorageType storageType = (dataSource.offlineStore ?
//                                                BECurrentObjectStorageTypeOfflineStore :
//                                                BECurrentObjectStorageTypeFile);
//      _currentUserController = [BECurrentUserController controllerWithStorageType:storageType
//                                                                 commonDataSource:dataSource
//                                                                   coreDataSource:self];
//    }
//    controller = _currentUserController;
//  });
//  return controller;
//}

- (void)setCurrentUserController:(BECurrentUserController *)currentUserController {
  dispatch_sync(_controllerAccessQueue, ^{
    _currentUserController = currentUserController;
  });
}

#if !TARGET_OS_WATCH && !TARGET_OS_TV

///--------------------------------------
#pragma mark - Installation Controller
///--------------------------------------

//- (BEInstallationController *)installationController {
//  __block BEInstallationController *controller = nil;
//  dispatch_sync(_controllerAccessQueue, ^{
//    if (!_installationController) {
//      _installationController = [BEInstallationController controllerWithDataSource:self];
//    }
//    controller = _installationController;
//  });
//  return controller;
//}
//
//- (void)setInstallationController:(BEInstallationController *)installationController {
//  dispatch_sync(_controllerAccessQueue, ^{
//    _installationController = installationController;
//  });
//}

#endif

///--------------------------------------
#pragma mark - User Controller
///--------------------------------------

- (BEUserController *)userController {
  __block BEUserController *controller = nil;
  dispatch_sync(_controllerAccessQueue, ^{
    if (!_userController) {
      _userController = [BEUserController controllerWithCommonDataSource:self.dataSource
                                                          coreDataSource:self];
    }
    controller = _userController;
  });
  return controller;
}

- (void)setUserController:(BEUserController *)userController {
  dispatch_sync(_controllerAccessQueue, ^{
    _userController = userController;
  });
}

@end
