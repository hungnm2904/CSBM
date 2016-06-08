//
//  BECurrentInstallationController.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BECurrentInstallationController.h"
#import "BFTask+Private.h"
#import "BEAsyncTaskQueue.h"
#import "BEInstallationIdentifierStore.h"
#import "BEInstallationPrivate.h"
#import "BEMacros.h"
#import "BEObjectPrivate.h"
#import "BEObject.h"
#import "BEInstallation.h"

NSString *const BECurrentInstallationFileName = @"currentInstallation";
NSString *const BECurrentInstallationPinName = @"_currentInstallation";

@interface BECurrentInstallationController () {
  dispatch_queue_t _dataQueue;
  BEAsyncTaskQueue *_dataTaskQueue;
}

//@property (nonatomic, strong, readonly) BEFileManager *fileManager;
@property (nonatomic, strong, readonly) BEInstallationIdentifierStore *installationIdentifierStore;

@property (nonatomic, strong) BEInstallation *currentInstallation;
@property (nonatomic, assign) BOOL currentInstallationMatchesDisk;

@end

@implementation BECurrentInstallationController

@synthesize storageType = _storageType;

@synthesize currentInstallation = _currentInstallation;
@synthesize currentInstallationMatchesDisk = _currentInstallationMatchesDisk;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

//- (instancetype)initWithStorageType:(BECurrentObjectStorageType)storageType
//                   commonDataSource:(id<BEInstallationIdentifierStoreProvider>)commonDataSource
//                     coreDataSource:(id<BEObjectFilePersistenceControllerProvider>)coreDataSource {
//  self = [super init];
//  if (!self) return nil;
//  
//  _dataQueue = dispatch_queue_create("com.parse.installation.current", DISPATCH_QUEUE_CONCURRENT);
//  _dataTaskQueue = [[BEAsyncTaskQueue alloc] init];
//  
//  _storageType = storageType;
//  _commonDataSource = commonDataSource;
//  _coreDataSource = coreDataSource;
//  
//  return self;
//}
//
//+ (instancetype)controllerWithStorageType:(BECurrentObjectStorageType)storageType
//                         commonDataSource:(id<BEInstallationIdentifierStoreProvider>)commonDataSource
//                           coreDataSource:(id<BEObjectFilePersistenceControllerProvider>)coreDataSource {
//  return [[self alloc] initWithStorageType:storageType
//                          commonDataSource:commonDataSource
//                            coreDataSource:coreDataSource];
//}

///--------------------------------------
#pragma mark - BECurrentObjectControlling
///--------------------------------------

//- (BFTask *)getCurrentObjectAsync {
//  @weakify(self);
//  return [_dataTaskQueue enqueue:^BFTask *(BFTask *unused) {
//    return [[[BFTask taskFromExecutor:[BFExecutor defaultExecutor] withBlock:^id {
//      @strongify(self);
//      if (self.currentInstallation) {
//        return self.currentInstallation;
//      }
//      
//      if (!self.currentInstallationMatchesDisk) {
//        return [[self _loadCurrentInstallationFromDiskAsync] continueWithBlock:^id(BFTask *task) {
//          BEInstallation *installation = task.result;
//          if (installation) {
//            // If there is no objectId, but there is some data
//            // it means that the data wasn't yet saved to the server
//            // so we should mark everything as dirty
//            if (!installation.objectId && installation.allKeys.count) {
//              [installation _markAllFieldsDirty];
//            }
//          }
//          return task;
//        }];
//      }
//      return nil;
//    }] continueWithBlock:^id(BFTask *task) {
//      @strongify(self);
//      
//      __block BEInstallation *installation = task.result;
//      return [[self.installationIdentifierStore getInstallationIdentifierAsync] continueWithSuccessBlock:^id _Nullable(BFTask<NSString *> * _Nonnull task) {
//        NSString *installationId = task.result.lowercaseString;
//        if (!installation || ![installationId isEqualToString:installation.installationId]) {
//          // If there's no installation object, or the object's installation
//          // ID doesn't match this device's installation ID, create a new
//          // installation. Try to keep track of the previously stored device
//          // token: if there was an installation already stored just re-use
//          // its device token, otherwise try loading from the keychain (where
//          // old SDKs stored the token). Discard the old installation.
//          NSString *oldDeviceToken = nil;
//          if (installation) {
//            oldDeviceToken = installation.deviceToken;
//          } else {
//            oldDeviceToken = [[BEPush pushInternalUtilClass] getDeviceTokenFromKeychain];
//          }
//          
//          installation = [BEInstallation object];
//          installation.deviceType = kBEDeviceType;
//          installation.installationId = installationId;
//          if (oldDeviceToken) {
//            installation.deviceToken = oldDeviceToken;
//          }
//        }
//        return installation;
//      }];
//    }] continueWithBlock:^id(BFTask *task) {
//      dispatch_barrier_sync(_dataQueue, ^{
//        _currentInstallation = task.result;
//        _currentInstallationMatchesDisk = !task.faulted;
//      });
//      return task;
//    }];
//  }];
//}

//- (BFTask *)saveCurrentObjectAsync:(BEObject *)object {
//  BEInstallation *installation = (BEInstallation *)object;
//  
//  @weakify(self);
//  return [_dataTaskQueue enqueue:^BFTask *(BFTask *unused) {
//    @strongify(self);
//    
//    if (installation != self.currentInstallation) {
//      return nil;
//    }
//    return [[self _saveCurrentInstallationToDiskAsync:installation] continueWithBlock:^id(BFTask *task) {
//      self.currentInstallationMatchesDisk = (!task.faulted && !task.cancelled);
//      return nil;
//    }];
//  }];
//}

///--------------------------------------
#pragma mark - Installation
///--------------------------------------

- (BEInstallation *)memoryCachedCurrentInstallation {
  return self.currentInstallation;
}

//- (BFTask *)clearCurrentInstallationAsync {
//  @weakify(self);
//  return [_dataTaskQueue enqueue:^BFTask *(BFTask *unused) {
//    @strongify(self);
//    
//    dispatch_barrier_sync(_dataQueue, ^{
//      _currentInstallation = nil;
//      _currentInstallationMatchesDisk = NO;
//    });
//    
//    NSMutableArray *tasks = [NSMutableArray arrayWithCapacity:2];
//    if (self.storageType == BECurrentObjectStorageTypeOfflineStore) {
//      BFTask *unpinTask = [BEObject unpinAllObjectsInBackgroundWithName:BECurrentInstallationPinName];
//      [tasks addObject:unpinTask];
//    }
//    
//    BFTask *fileTask = [self.coreDataSource.objectFilePersistenceController removePersistentObjectAsyncForKey:BECurrentInstallationFileName];
//    [tasks addObject:fileTask];
//    
//    return [BFTask taskForCompletionOfAllTasks:tasks];
//  }];
//}

- (BFTask *)clearMemoryCachedCurrentInstallationAsync {
  return [_dataTaskQueue enqueue:^BFTask *(BFTask *unused) {
    self.currentInstallation = nil;
    self.currentInstallationMatchesDisk = NO;
    
    return nil;
  }];
}

///--------------------------------------
#pragma mark - Data Storage
///--------------------------------------

//- (BFTask *)_loadCurrentInstallationFromDiskAsync {
//  if (self.storageType == BECurrentObjectStorageTypeOfflineStore) {
//    // Try loading from OfflineStore
//    BEQuery *query = [[[BEQuery queryWithClassName:[BEInstallation csbmClassName]]
//                       fromPinWithName:BECurrentInstallationPinName] ignoreACLs];
//    
//    return [[query findObjectsInBackground] continueWithSuccessBlock:^id(BFTask *task) {
//      NSArray *results = task.result;
//      if (results.count == 1) {
//        return [BFTask taskWithResult:results.firstObject];
//      } else if (results.count != 0) {
//        return [[BEObject unpinAllObjectsInBackgroundWithName:BECurrentInstallationPinName]
//                continueWithSuccessResult:nil];
//      }
//      
//      // Backward compatibility if we previously have non-LDS currentInstallation.
//      return [BEObject _migrateObjectInBackgroundFromFile:BECurrentInstallationFileName
//                                                    toPin:BECurrentInstallationPinName];
//    }];
//  }
//  
//  BEObjectFilePersistenceController *controller = self.objectFilePersistenceController;
//  return [controller loadPersistentObjectAsyncForKey:BECurrentInstallationFileName];
//}

//- (BFTask *)_saveCurrentInstallationToDiskAsync:(BEInstallation *)installation {
//  if (self.storageType == BECurrentObjectStorageTypeOfflineStore) {
//    BFTask *task = [BEObject unpinAllObjectsInBackgroundWithName:BECurrentInstallationPinName];
//    return [task continueWithBlock:^id(BFTask *task) {
//      // Make sure to not pin children of BEInstallation automatically, as it can create problems
//      // if any of the children are of Installation class.
//      return [installation _pinInBackgroundWithName:BECurrentInstallationPinName includeChildren:NO];
//    }];
//  }
//  
//  BEObjectFilePersistenceController *controller = self.objectFilePersistenceController;
//  return [controller persistObjectAsync:installation forKey:BECurrentInstallationFileName];
//}

///--------------------------------------
#pragma mark - Accessors
///--------------------------------------

//- (BEObjectFilePersistenceController *)objectFilePersistenceController {
//  return self.coreDataSource.objectFilePersistenceController;
//}

- (BEInstallationIdentifierStore *)installationIdentifierStore {
  return self.commonDataSource.installationIdentifierStore;
}

- (BEInstallation *)currentInstallation {
  __block BEInstallation *installation = nil;
  dispatch_sync(_dataQueue, ^{
    installation = _currentInstallation;
  });
  return installation;
}

- (void)setCurrentInstallation:(BEInstallation *)currentInstallation {
  dispatch_barrier_sync(_dataQueue, ^{
    if (_currentInstallation != currentInstallation) {
      _currentInstallation = currentInstallation;
    }
  });
}

- (BOOL)currentInstallationMatchesDisk {
  __block BOOL matches = NO;
  dispatch_sync(_dataQueue, ^{
    matches = _currentInstallationMatchesDisk;
  });
  return matches;
}

- (void)setCurrentInstallationMatchesDisk:(BOOL)currentInstallationMatchesDisk {
  dispatch_barrier_sync(_dataQueue, ^{
    _currentInstallationMatchesDisk = currentInstallationMatchesDisk;
  });
}

@end
