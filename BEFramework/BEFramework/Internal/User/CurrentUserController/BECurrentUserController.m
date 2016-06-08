//
//  BECurrentUserController.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BECurrentUserController.h"

#import <Bolts/BFTaskCompletionSource.h>
#import "BFTask+Private.h"
#import "BEAssert.h"
#import "BEAnonymousUtils_Private.h"
#import "BEAsyncTaskQueue.h"
#import "BEMutableUserState.h"
#import "BEObjectPrivate.h"
#import "BEQuery.h"
#import "BEUserConstants.h"
#import "BEUserPrivate.h"
#import "BEAnonymousUtils.h"
#import "BEObjectState.h"

@interface BECurrentUserController () {
  dispatch_queue_t _dataQueue;
  BEAsyncTaskQueue *_dataTaskQueue;
  
  BEUser *_currentUser;
  BOOL _currentUserMatchesDisk;
}

@end

@implementation BECurrentUserController
@synthesize storageType = _storageType;

///--------------------------------------
#pragma mark - BECurrentObjectControlling
///--------------------------------------

//- (BFTask *)getCurrentObjectAsync {
//  BECurrentUserLoadingOptions options = 0;
//  if (self.automaticUsersEnabled) {
//    options |= BECurrentUserLoadingOptionCreateLazyIfNotAvailable;
//  }
//  return [self getCurrentUserAsyncWithOptions:options];
//}
//
//- (BFTask *)saveCurrentObjectAsync:(BEObject *)object {
//  BEUser *user = (BEUser *)object;
//  return [_dataTaskQueue enqueue:^id(BFTask *task) {
//    return [self _saveCurrentUserAsync:user];
//  }];
//}

///--------------------------------------
#pragma mark - User
///--------------------------------------

//- (BFTask *)getCurrentUserAsyncWithOptions:(BECurrentUserLoadingOptions)options {
//  return [_dataTaskQueue enqueue:^id(BFTask *task) {
//    return [self _getCurrentUserAsyncWithOptions:options];
//  }];
//}
//
//- (BFTask *)_getCurrentUserAsyncWithOptions:(BECurrentUserLoadingOptions)options {
//  return [BFTask taskFromExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:^id{
//    __block BOOL matchesDisk = NO;
//    __block BEUser *currentUser = nil;
//    dispatch_sync(_dataQueue, ^{
//      matchesDisk = _currentUserMatchesDisk;
//      currentUser = _currentUser;
//    });
//    if (currentUser) {
//      return currentUser;
//    }
//    
//    if (matchesDisk) {
//      if (options & BECurrentUserLoadingOptionCreateLazyIfNotAvailable) {
//        return [self _lazyLogInUser];
//      }
//      return nil;
//    }
//    
//    return [[[[self _loadCurrentUserFromDiskAsync] continueWithSuccessBlock:^id(BFTask *task) {
//      BEUser *user = task.result;
//      // If the object was not yet saved, but is already linked with AnonymousUtils - it means it is lazy.
//      // So mark it's state as `lazy` and make it `dirty`
//      if (!user.objectId && [BEAnonymousUtils isLinkedWithUser:user]) {
//        user._lazy = YES;
//        [user _setDirty:YES];
//      }
//      return user;
//    }] continueWithBlock:^id(BFTask *task) {
//      dispatch_barrier_sync(_dataQueue, ^{
//        _currentUser = task.result;
//        _currentUserMatchesDisk = !task.faulted;
//      });
//      return task;
//    }] continueWithBlock:^id(BFTask *task) {
//      // If there's no user and automatic user is enabled, do lazy login.
//      if (!task.result && (options & BECurrentUserLoadingOptionCreateLazyIfNotAvailable)) {
//        return [self _lazyLogInUser];
//      }
//      return task;
//    }];
//  }];
//}
//
//- (BFTask *)_loadCurrentUserFromDiskAsync {
//  BFTask *task = nil;
//  if (self.storageType == BECurrentObjectStorageTypeOfflineStore) {
//    // Try loading from OfflineStore
//    BEQuery *query = [[[BEQuery queryWithClassName:[BEUser csbmClassName]]
//                       fromPinWithName:BEUserCurrentUserPinName]
//                      // We need to ignoreACLs here because right now we don't have currentUser.
//                      ignoreACLs];
//    
//    // Silence the warning if we are loading from LDS
//    task = [[query findObjectsInBackground] continueWithSuccessBlock:^id(BFTask *task) {
//      NSArray *results = task.result;
//      if (results.count == 1) {
//        return results.firstObject;
//      } else if (results.count != 0) {
//        return [[BEObject unpinAllObjectsInBackgroundWithName:BEUserCurrentUserPinName] continueWithSuccessResult:nil];
//      }
//      
//      // Backward compatibility if we previously have non-LDS currentUser.
//      return [BEObject _migrateObjectInBackgroundFromFile:BEUserCurrentUserFileName toPin:BEUserCurrentUserPinName usingMigrationBlock:^id(BFTask *task) {
//        BEUser *user = task.result;
//        // Only migrate session token to Keychain if it was loaded from Data File.
//        if (user.sessionToken) {
//          return [self _saveSensitiveUserDataAsync:user
//                            toKeychainItemWithName:BEUserCurrentUserKeychainItemName];
//        }
//        return nil;
//      }];
//    }];
//  } else {
////    BEObjectFilePersistenceController *controller = self.coreDataSource.objectFilePersistenceController;
////    task = [controller loadPersistentObjectAsyncForKey:BEUserCurrentUserFileName];
//  }
//  return [task continueWithSuccessBlock:^id(BFTask *task) {
//    BEUser *user = task.result;
//    user._current = YES;
//    return [[self _loadSensitiveUserDataAsync:user
//                     fromKeychainItemWithName:BEUserCurrentUserKeychainItemName] continueWithSuccessResult:user];
//  }];
//}
//- (BFTask *)_saveCurrentUserAsync:(BEUser *)user {
//  return [BFTask taskFromExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:^id{
//    __block BEUser *currentUser = nil;
//    dispatch_sync(_dataQueue, ^{
//      currentUser = _currentUser;
//    });
//    
//    BFTask *task = [BFTask taskWithResult:nil];
//    // Check for objectId equality to not logout in case we are saving another instance of the same user.
//    if (currentUser != nil && currentUser != user && ![user.objectId isEqualToString:currentUser.objectId]) {
//      task = [task continueWithBlock:^id(BFTask *task) {
//        return [currentUser _logOutAsync];
//      }];
//    }
//    return [[task continueWithBlock:^id(BFTask *task) {
//      @synchronized (user.lock) {
//        user._current = YES;
//        [user synchronizeAllAuthData];
//      }
//      return [self _saveCurrentUserToDiskAsync:user];
//    }] continueWithBlock:^id(BFTask *task) {
//      dispatch_barrier_sync(_dataQueue, ^{
//        _currentUser = user;
//        _currentUserMatchesDisk = !task.faulted && !task.cancelled;
//      });
//      return user;
//    }];
//  }];
//}
//
//- (BFTask *)logOutCurrentUserAsync {
//  return [_dataTaskQueue enqueue:^id(BFTask *task) {
//    return [[self _getCurrentUserAsyncWithOptions:0] continueWithBlock:^id(BFTask *task) {
//      BFTask *userLogoutTask = nil;
//      
//      BEUser *user = task.result;
//      if (user) {
//        userLogoutTask = [user _logOutAsync];
//      } else {
//        userLogoutTask = [BFTask taskWithResult:nil];
//      }
//      
//      //BFTask *fileTask = [self.coreDataSource.objectFilePersistenceController removePersistentObjectAsyncForKey:BEUserCurrentUserFileName];
//      BFTask *unpinTask = nil;
//      
//      if (self.storageType == BECurrentObjectStorageTypeOfflineStore) {
//        unpinTask = [BEObject unpinAllObjectsInBackgroundWithName:BEUserCurrentUserPinName];
//      } else {
//        unpinTask = [BFTask taskWithResult:nil];
//      }
//      
//      [self _deleteSensitiveUserDataFromKeychainWithItemName:BEUserCurrentUserFileName];
//      
//      BFTask *logoutTask = [[BFTask taskForCompletionOfAllTasks:@[ fileTask, unpinTask ]] continueWithBlock:^id(BFTask *task) {
//        dispatch_barrier_sync(_dataQueue, ^{
//          _currentUser = nil;
//          _currentUserMatchesDisk = YES;
//        });
//        return nil;
//      }];
//      return [BFTask taskForCompletionOfAllTasks:@[ userLogoutTask, logoutTask ]];
//    }];
//  }];
//}


///--------------------------------------
#pragma mark - Session Token
///--------------------------------------

- (BFTask *)getCurrentUserSessionTokenAsync {
  return [[self getCurrentUserAsyncWithOptions:0] continueWithSuccessBlock:^id(BFTask *task) {
    BEUser *user = task.result;
    return user.sessionToken;
  }];
}

///--------------------------------------
#pragma mark - Lazy Login
///--------------------------------------

//- (BFTask *)_lazyLogInUser {
//  return [BFTask taskFromExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:^id{
//    BEUser *user = [BEAnonymousUtils _lazyLogIn];
//    
//    // When LDS is enabled, we will immediately save the anon user to LDS. When LDS is disabled, we
//    // will create the anon user, but will lazily save it to Parse on an object save that has this
//    // user in its ACL.
//    // The main differences here would be that non-LDS may have different anon users in different
//    // sessions until an object is saved and LDS will persist the same anon user. This shouldn't be a
//    // big deal...
//    if (self.storageType == BECurrentObjectStorageTypeOfflineStore) {
//      return [[self _saveCurrentUserAsync:user] continueWithSuccessResult:user];
//    }
//    
//    dispatch_barrier_sync(_dataQueue, ^{
//      _currentUser = user;
//      _currentUserMatchesDisk = YES;
//    });
//    return user;
//  }];
//}
@end
