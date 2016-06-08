//
//  BEUser.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEUser.h"
#import "BEUserPrivate.h"
#import "BEUser+Synchronous.h"
#import "BEObject+Synchronous.h"

#import <Bolts/BFExecutor.h>
#import <Bolts/BFTaskCompletionSource.h>

#import "BFTask+Private.h"
#import "BEAnonymousAuthenticationProvider.h"
#import "BEAnonymousUtils_Private.h"
#import "BEAssert.h"
#import "BECommandResult.h"
#import "BECoreManager.h"
#import "BECommandRunning.h"
#import "BEDecoder.h"
#import "BEErrorUtilities.h"
#import "BEMutableUserState.h"
#import "BEObject+Subclass.h"
#import "BEObjectConstant.h"
#import "BEObjectPrivate.h"
#import "BEOperationSet.h"
#import "BEQueryPrivate.h"
#import "BERESTUserCommand.h"
#import "BESessionUtitlites.h"
#import "BETaskQueue.h"
#import "BEUserAuthenticationController.h"
#import "BEUserConstants.h"
#import "BEUserController.h"
#import "BEUserFileCodingLogic.h"
#import "CSBM_Private.h"

NSString *const BEUserCurrentUserFileName = @"currentUser";
NSString *const BEUserCurrentUserPinName = @"_currentUser";
NSString *const BEUserCurrentUserKeychainItemName = @"currentUser";

static BOOL _BEUserIsWritablePropertyForKey(NSString *key) {
  return ![BEUserSessionTokenRESTKey isEqualToString:key];
}

static BOOL _BEUserIsRemovablePropertyForKey(NSString *key) {
  return _BEUserIsWritablePropertyForKey(key) && ![BEUserUsernameRESTKey isEqualToString:key];
}

@interface BEUser () <BEObjectPrivateSubclass>

@property (nonatomic, copy) BEUserState *_state;

@end

@implementation BEUser (Private)
- (void)setDefaultValues {
  [super setDefaultValues];
  self._current = NO;
}

- (BOOL)needsDefaultACL {
  return NO;
}

///--------------------------------------
#pragma mark - Current User
///--------------------------------------

// Returns the session token for the current user.
+ (NSString *)currentSessionToken {
  return [[self _getCurrentUserSessionTokenAsync] waitForResult:nil withMainThreadWarning:NO];
}

//+ (BFTask *)_getCurrentUserSessionTokenAsync {
//  return [[self currentUserController] getCurrentUserSessionTokenAsync];
//}

///--------------------------------------
#pragma mark - BEObject
///--------------------------------------

#pragma mark Validation

- (BFTask<BEVoid> *)_validateDeleteAsync {
  return [[super _validateDeleteAsync] continueWithSuccessBlock:^id(BFTask<BEVoid> *task) {
    if (!self.authenticated) {
      NSError *error = [BEErrorUtilities errorWithCode:kBEErrorUserCannotBeAlteredWithoutSession
                                               message:@"User cannot be deleted unless they have been authenticated."];
      return [BFTask taskWithError:error];
    }
    return nil;
  }];
}

- (BFTask<BEVoid> *)_validateSaveEventuallyAsync {
  return [[super _validateSaveEventuallyAsync] continueWithSuccessBlock:^id(BFTask<BEVoid> *task) {
    if ([self isDirtyForKey:BEUserPasswordRESTKey]) {
      NSError *error = [BEErrorUtilities errorWithCode:kBEErrorOperationForbidden
                                               message:@"Unable to saveEventually a BEUser with dirty password."];
      return [BFTask taskWithError:error];
    }
    return nil;
  }];
}

#pragma mark Else

- (NSString *)displayClassName {
  if ([self isMemberOfClass:[BEUser class]]) {
    return @"BEUser";
  }
  return NSStringFromClass([self class]);
}

// Validates a class name. We override this to only allow the user class name.
+ (void)_assertValidInstanceClassName:(NSString *)className {
  BEParameterAssert([className isEqualToString:[BEUser csbmClassName]],
                    @"Cannot initialize a BEUser with a custom class name.");
}

// Checks the properties on the object before saving.
- (void)_checkSaveParametersWithCurrentUser:(BEUser *)currentUser {
  @synchronized([self lock]) {
    BEConsistencyAssert(self.objectId || self._lazy,
                        @"User cannot be saved unless they are already signed up. Call signUp first.");
    
    BEConsistencyAssert([self _isAuthenticatedWithCurrentUser:currentUser] ||
                        [self.objectId isEqualToString:currentUser.objectId],
                        @"User cannot be saved unless they have been authenticated via logIn or signUp", nil);
  }
}

// Checks the properties on the object before signUp.
- (BFTask *)_validateSignUpAsync {
  return [BFTask taskFromExecutor:[BFExecutor defaultExecutor] withBlock:^id {
    NSError *error = nil;
    @synchronized (self.lock) {
      if (!self.username) {
        error = [BEErrorUtilities errorWithCode:kBEErrorUsernameMissing
                                        message:@"Cannot sign up without a username."];
      } else if (!self.password) {
        error = [BEErrorUtilities errorWithCode:kBEErrorUserPasswordMissing
                                        message:@"Cannot sign up without a password."];
      } else if (![self isDirty:NO] || self.objectId) {
        error = [BEErrorUtilities errorWithCode:kBEErrorUsernameTaken
                                        message:@"Cannot sign up an existing user."];
      }
    }
    if (error) {
      return [BFTask taskWithError:error];
    }
    return nil;
  }];
}

- (NSMutableDictionary *)_convertToDictionaryForSaving:(BEOperationSet *)changes
                                     withObjectEncoder:(BEEncoder *)encoder {
  @synchronized([self lock]) {
    NSMutableDictionary *serialized = [super _convertToDictionaryForSaving:changes withObjectEncoder:encoder];
    if (self.authData.count > 0) {
      serialized[BEUserAuthDataRESTKey] = [self.authData copy];
    }
    return serialized;
  }
}

//- (BFTask *)handleSaveResultAsync:(NSDictionary *)result {
//  return [[super handleSaveResultAsync:result] continueWithSuccessBlock:^id(BFTask *saveTask) {
//    if (self._current) {
//      [self cleanUpAuthData];
//      BECurrentUserController *controller = [[self class] currentUserController];
//      return [[controller saveCurrentObjectAsync:self] continueWithBlock:^id(BFTask *task) {
//        return saveTask.result;
//      }];
//    }
//    return saveTask;
//  }];
//}

///--------------------------------------
#pragma mark - Sign Up
///--------------------------------------

- (BERESTCommand *)_currentSignUpCommandForChanges:(BEOperationSet *)changes {
  @synchronized([self lock]) {
    NSDictionary *parameters = [self _convertToDictionaryForSaving:changes
                                                 withObjectEncoder:[BEPointerObjectEncoder objectEncoder]];
    return [BERESTUserCommand signUpUserCommandWithParameters:parameters
                                             revocableSession:[[self class] _isRevocableSessionEnabled]
                                                 sessionToken:self.sessionToken];
  }
}

///--------------------------------------
#pragma mark - Service Login
///--------------------------------------

// Constructs the command for user_signup_or_login. This is used for Facebook, Twitter, and other linking services.
- (BERESTCommand *)_currentServiceLoginCommandForChanges:(BEOperationSet *)changes {
  @synchronized([self lock]) {
    NSDictionary *parameters = [self _convertToDictionaryForSaving:changes
                                                 withObjectEncoder:[BEPointerObjectEncoder objectEncoder]];
    return [BERESTUserCommand serviceLoginUserCommandWithParameters:parameters
                                                   revocableSession:[[self class] _isRevocableSessionEnabled]
                                                       sessionToken:self.sessionToken];
  }
}

//- (BFTask *)_handleServiceLoginCommandResult:(BECommandResult *)result {
//  return [BFTask taskFromExecutor:[BFExecutor defaultExecutor] withBlock:^id {
//    NSDictionary *resultDictionary = result.result;
//    return [[self handleSaveResultAsync:resultDictionary] continueWithBlock:^id(BFTask *task) {
//      BOOL new = (result.httpResponse.statusCode == 201); // 201 means Created
//      @synchronized (self.lock) {
//        if (self._state.isNew != new) {
//          self._state = [self._state copyByMutatingWithBlock:^(BEMutableUserState *state) {
//            state.isNew = new;
//          }];
//        }
//        if (resultDictionary) {
//          self._lazy = NO;
//          
//          // Serialize the object to disk so we can later access it via currentUser
//          BECurrentUserController *controller = [[self class] currentUserController];
//          return [[controller saveCurrentObjectAsync:self] continueAsyncWithBlock:^id(BFTask *task) {
//            [self.saveDelegate invoke:self error:nil];
//            return self;
//          }];
//        }
//        return self;
//      }
//    }];
//  }];
//}
// Override the save result handling with custom user functionality
//- (BFTask *)handleSignUpResultAsync:(BFTask *)task {
//  @synchronized([self lock]) {
//    BECommandResult *commandResult = task.result;
//    NSDictionary *result = commandResult.result;
//    BFTask *signUpTask = task;
//    
//    // Bail-out early, but still make sure that super class handled the result
//    if (task.error || task.cancelled || task.exception) {
//      return [[super handleSaveResultAsync:nil] continueWithBlock:^id(BFTask *task) {
//        return signUpTask;
//      }];
//    }
//    __block BOOL saveResult = NO;
//    return [[[super handleSaveResultAsync:result] continueWithBlock:^id(BFTask *task) {
//      saveResult = [task.result boolValue];
//      if (saveResult) {
//        @synchronized (self.lock) {
//          // Save the session information
//          self._state = [self._state copyByMutatingWithBlock:^(BEMutableUserState *state) {
//            state.sessionToken = result[BEUserSessionTokenRESTKey];
//            state.isNew = YES;
//          }];
//          self._lazy = NO;
//        }
//      }
//      return signUpTask;
//    }] continueWithBlock:^id(BFTask *task) {
//      BECurrentUserController *controller = [[self class] currentUserController];
//      return [[controller saveCurrentObjectAsync:self] continueWithResult:@(saveResult)];
//    }];
//  }
//}

//- (void)cleanUpAuthData {
//  @synchronized([self lock]) {
//    for (NSString *key in [self.authData copy]) {
//      id linkData = self.authData[key];
//      if (!linkData || linkData == [NSNull null]) {
//        [self.authData removeObjectForKey:key];
//        [self.linkedServiceNames removeObject:key];
//        
//        [[[[self class] authenticationController] restoreAuthenticationAsyncWithAuthData:nil
//                                                                             forAuthType:key] waitForResult:nil withMainThreadWarning:NO];
//      }
//    }
//  }
//}
/**
 Copies special BEUser fields from another user.
 */
- (BEObject *)mergeFromObject:(BEUser *)other {
  @synchronized([self lock]) {
    [super mergeFromObject:other];
    
    if (self == other) {
      // If they point to the same instance, then don't merge.
      return self;
    }
    
    self._state = [self._state copyByMutatingWithBlock:^(BEMutableUserState *state) {
      state.sessionToken = other.sessionToken;
      state.isNew = other._state.isNew;
    }];
    
    [self.authData removeAllObjects];
    [self.authData addEntriesFromDictionary:other.authData];
    
    [self.linkedServiceNames removeAllObjects];
    [self.linkedServiceNames unionSet:other.linkedServiceNames];
    
    return self;
  }
}

- (void)_mergeFromServerWithResult:(NSDictionary *)result decoder:(BEDecoder *)decoder completeData:(BOOL)completeData {
  @synchronized([self lock]) {
    // save the session token
    
    NSString *newSessionToken = result[BEUserSessionTokenRESTKey];
    if (newSessionToken) {
      // Save the session token
      self._state = [self._state copyByMutatingWithBlock:^(BEMutableUserState *state) {
        state.sessionToken = newSessionToken;
      }];
    }
    
    // Merge the linked service metadata
    NSDictionary *newAuthData = [decoder decodeObject:result[BEUserAuthDataRESTKey]];
    if (newAuthData) {
      [self.authData removeAllObjects];
      [self.linkedServiceNames removeAllObjects];
      [newAuthData enumerateKeysAndObjectsUsingBlock:^(id key, id linkData, BOOL *stop) {
        if (linkData != [NSNull null]) {
          self.authData[key] = linkData;
          [self.linkedServiceNames addObject:key];
          [self synchronizeAuthDataWithAuthType:key];
        } else {
          [self.authData removeObjectForKey:key];
          [self.linkedServiceNames removeObject:key];
          [self synchronizeAuthDataWithAuthType:key];
        }
      }];
    }
    
    // Strip authData and sessionToken from the data, as those keys are saved in a custom way
    NSMutableDictionary *serverData = [result mutableCopy];
    [serverData removeObjectForKey:BEUserSessionTokenRESTKey];
    [serverData removeObjectForKey:BEUserAuthDataRESTKey];
    
    // The public fields are handled by the regular mergeFromServer
    [super _mergeFromServerWithResult:serverData decoder:decoder completeData:completeData];
  }
}
//- (void)synchronizeAuthDataWithAuthType:(NSString *)authType {
//  @synchronized([self lock]) {
//    if (!self._current) {
//      return;
//    }
//    
//    NSDictionary *data = self.authData[authType];
//    BFTask *restoreTask = [[[self class] authenticationController] restoreAuthenticationAsyncWithAuthData:data
//                                                                                              forAuthType:authType];
//    [restoreTask waitForResult:nil withMainThreadWarning:NO];
//    if (restoreTask.faulted || ![restoreTask.result boolValue]) { // TODO: (nlutsenko) Maybe chain this method?
//      [self unlinkWithAuthTypeInBackground:authType];
//    }
//  }
//}
- (void)synchronizeAllAuthData {
  @synchronized([self lock]) {
    // Ensures that all auth providers have auth data (e.g. access tokens, etc.) that matches this user.
    if (self.authData) {
      [self.authData enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
        [self synchronizeAuthDataWithAuthType:key];
      }];
    }
  }
}

//- (BFTask *)resolveLazinessAsync:(BFTask *)toAwait {
//  @synchronized([self lock]) {
//    if (!self._lazy) {
//      return [BFTask taskWithResult:self];
//    }
//    if (self.linkedServiceNames.count == 0) {
//      // If there are no linked services, treat this like a sign-up.
//      return [[self signUpAsync:toAwait] continueAsyncWithSuccessBlock:^id(BFTask *task) {
//        self._lazy = NO;
//        return self;
//      }];
//    }
//    
//    // Otherwise, treat this as a SignUpOrLogIn
//    BERESTCommand *command = [self _currentServiceLoginCommandForChanges:[self unsavedChanges]];
//    [self startSave];
//    
//    return [[toAwait continueAsyncWithBlock:^id(BFTask *task) {
//      return [[CSBM _currentManager].commandRunner runCommandAsync:command withOptions:0];
//    }] continueAsyncWithBlock:^id(BFTask *task) {
//      BECommandResult *result = task.result;
//      
//      if (task.error || task.cancelled) {
//        // If there was an error, we want to roll forward the save changes, but return the original task.
//        return [[self _handleServiceLoginCommandResult:result] continueAsyncWithBlock:^id(BFTask *unused) {
//          // Return the original task, instead of the new one (in order to have a proper error)
//          return task;
//        }];
//      }
//      
//      if (result.httpResponse.statusCode == 201) {
//        return [self _handleServiceLoginCommandResult:result];
//      } else {
//        // Otherwise, treat this as a fresh login, and switch the current user to the new user.
//        BEUser *newUser = [[self class] _objectFromDictionary:result.result
//                                             defaultClassName:self.CSBMClassName
//                                                 completeData:YES];
//        @synchronized ([newUser lock]) {
//          [newUser startSave];
//          return [newUser _handleServiceLoginCommandResult:result];
//        }
//      }
//    }];
//  }
//}
+ (instancetype)logInLazyUserWithAuthType:(NSString *)authType authData:(NSDictionary *)authData {
  BEUser *user = [self user];
  @synchronized([user lock]) {
    user._current = YES;
    user._lazy = YES;
    user.authData[authType] = authData;
    [user.linkedServiceNames addObject:authType];
  }
  return user;
}

//- (BFTask *)signUpAsync:(BFTask *)toAwait {
//  BEUser *currentUser = [[self class] currentUser];
//  NSString *token = currentUser.sessionToken;
//  @synchronized([self lock]) {
//    if (self.objectId) {
//      // For anonymous users, there may be an objectId.  Setting the userName
//      // will have removed the anonymous link and set the value in the authData
//      // object to [NSNull null], so we can just treat it like a save operation.
//      if (self.authData[BEAnonymousUserAuthenticationType] == [NSNull null]) {
//        return [self saveAsync:toAwait];
//      }
//      
//      // Otherwise, return an error
//      NSError *error = [BEErrorUtilities errorWithCode:kBEErrorUsernameTaken
//                                               message:@"Cannot sign up a user that has already signed up."];
//      return [BFTask taskWithError:error];
//    }
//    
//    // If the operationSetQueue is has operation sets in it, then a save or signUp is in progress.
//    // If there is a signUp or save already in progress, don't allow another one to start.
//    if ([self _hasOutstandingOperations]) {
//      NSError *error = [BEErrorUtilities errorWithCode:kBEErrorUsernameTaken
//                                               message:@"Cannot sign up a user that is already signing up."];
//      return [BFTask taskWithError:error];
//    }
//    
//    return [[self _validateSignUpAsync] continueWithSuccessBlock:^id(BFTask *task) {
//      if (currentUser && [BEAnonymousUtils isLinkedWithUser:currentUser]) {
//        // self doesn't have any outstanding saves, so we can safely merge its operations
//        // into the current user.
//        
//        BEConsistencyAssert(!self._current, @"Attempt to merge currentUser with itself.");
//        
//        @synchronized ([currentUser lock]) {
//          NSString *oldUsername = [currentUser.username copy];
//          NSString *oldPassword = [currentUser.password copy];
//          NSArray *oldAnonymousData = currentUser.authData[BEAnonymousUserAuthenticationType];
//          
//          // Move the changes to this object over to the currentUser object.
//          BEOperationSet *selfOperations = operationSetQueue[0];
//          [operationSetQueue removeAllObjects];
//          [operationSetQueue addObject:[[BEOperationSet alloc] init]];
//          for (NSString *key in selfOperations) {
//            currentUser[key] = selfOperations[key];
//          }
//          
//          currentUser->dirty = YES;
//          currentUser.password = self.password;
//          currentUser.username = self.username;
//          
//          [self rebuildEstimatedData];
//          [currentUser rebuildEstimatedData];
//          
//          return [[[[currentUser saveInBackground] continueWithBlock:^id(BFTask *task) {
//            if (task.error || task.cancelled || task.exception) {
//              @synchronized ([currentUser lock]) {
//                if (oldUsername) {
//                  currentUser.username = oldUsername;
//                }
//                currentUser.password = oldPassword;
//                [currentUser restoreAnonymity:oldAnonymousData];
//              }
//              
//              @synchronized(self.lock) {
//                operationSetQueue[0] = selfOperations;
//                [self rebuildEstimatedData];
//              }
//            }
//            return task;
//          }] continueWithSuccessBlock:^id(BFTask *task) {
//            if ([CSBM _currentManager].offlineStoreLoaded) {
//              return [[CSBM _currentManager].offlineStore deleteDataForObjectAsync:currentUser];
//            }
//            return nil;
//          }] continueWithSuccessBlock:^id(BFTask *task) {
//            [self mergeFromObject:currentUser];
//            BECurrentUserController *controller = [[self class] currentUserController];
//            return [[controller saveCurrentObjectAsync:self] continueWithResult:@YES];
//          }];
//        }
//      }
//      // Use a nil session token for objects saved during a signup.
//      BFTask *saveChildren = [self _saveChildrenInBackgroundWithCurrentUser:currentUser sessionToken:token];
//      BEOperationSet *changes = [self unsavedChanges];
//      [self startSave];
//      
//      return [[[toAwait continueWithBlock:^id(BFTask *task) {
//        return saveChildren;
//      }] continueWithSuccessBlock:^id(BFTask *task) {
//        // We need to construct the signup command lazily, because saving the children
//        // may change the way the object itself is serialized.
//        BERESTCommand *command = [self _currentSignUpCommandForChanges:changes];
//        return [[CSBM _currentManager].commandRunner runCommandAsync:command
//                                                          withOptions:BECommandRunningOptionRetryIfFailed];
//      }] continueWithBlock:^id(BFTask *task) {
//        return [self handleSignUpResultAsync:task];
//      }];
//    }];
//  }
//}
- (void)stripAnonymity {
  @synchronized([self lock]) {
    if ([BEAnonymousUtils isLinkedWithUser:self]) {
      NSString *authType = BEAnonymousUserAuthenticationType;
      
      [self.linkedServiceNames removeObject:authType];
      
      if (self.objectId) {
        self.authData[authType] = [NSNull null];
      } else {
        [self.authData removeObjectForKey:authType];
      }
      dirty = YES;
    }
  }
}

- (void)restoreAnonymity:(id)anonymousData {
  @synchronized([self lock]) {
    if (anonymousData && anonymousData != [NSNull null]) {
      NSString *authType = BEAnonymousUserAuthenticationType;
      [self.linkedServiceNames addObject:authType];
      self.authData[authType] = anonymousData;
    }
  }
}

///--------------------------------------
#pragma mark - Saving
///--------------------------------------

- (BERESTCommand *)_constructSaveCommandForChanges:(BEOperationSet *)changes
                                      sessionToken:(NSString *)token
                                     objectEncoder:(BEEncoder *)encoder {
  // If we are curent user - use the latest available session token, as it might have been changed since
  // this command was enqueued.
  if (self._current) {
    token = self.sessionToken;
  }
  return [super _constructSaveCommandForChanges:changes
                                   sessionToken:token
                                  objectEncoder:encoder];
}
///--------------------------------------
#pragma mark - REST operations
///--------------------------------------

- (void)mergeFromRESTDictionary:(NSDictionary *)object withDecoder:(BEDecoder *)decoder {
  @synchronized([self lock]) {
    NSMutableDictionary *restDictionary = [object mutableCopy];
    
    BEMutableUserState *state = [self._state mutableCopy];
    if (object[BEUserSessionTokenRESTKey] != nil) {
      state.sessionToken = object[BEUserSessionTokenRESTKey];
      [restDictionary removeObjectForKey:BEUserSessionTokenRESTKey];
    }
    
    if (object[BEUserAuthDataRESTKey] != nil) {
      NSDictionary *newAuthData = object[BEUserAuthDataRESTKey];
      [newAuthData enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
        self.authData[key] = obj;
        if (obj != nil) {
          [self.linkedServiceNames addObject:key];
        }
        [self synchronizeAuthDataWithAuthType:key];
      }];
      
      [restDictionary removeObjectForKey:BEUserAuthDataRESTKey];
    }
    
    self._state = state;
    
    [super mergeFromRESTDictionary:restDictionary withDecoder:decoder];
  }
}

- (NSDictionary *)RESTDictionaryWithObjectEncoder:(BEEncoder *)objectEncoder
                                operationSetUUIDs:(NSArray **)operationSetUUIDs
                                            state:(BEObjectState *)state
                                operationSetQueue:(NSArray *)queue
                          deletingEventuallyCount:(NSUInteger)deletingEventuallyCount {
  NSMutableArray *cleanQueue = [queue mutableCopy];
  [queue enumerateObjectsUsingBlock:^(BEOperationSet *operationSet, NSUInteger idx, BOOL *stop) {
    // Remove operations for `password` field, to not let it persist to LDS.
    if (operationSet[BEUserPasswordRESTKey]) {
      operationSet = [operationSet copy];
      [operationSet removeObjectForKey:BEUserPasswordRESTKey];
      
      cleanQueue[idx] = operationSet;
    }
  }];
  return [super RESTDictionaryWithObjectEncoder:objectEncoder
                              operationSetUUIDs:operationSetUUIDs
                                          state:state
                              operationSetQueue:cleanQueue
                        deletingEventuallyCount:deletingEventuallyCount];
}
///--------------------------------------
#pragma mark - Revocable Session
///--------------------------------------

+ (dispatch_queue_t)_revocableSessionSynchronizationQueue {
  static dispatch_queue_t queue;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    queue = dispatch_queue_create("com.CSBM.user.revocableSession", DISPATCH_QUEUE_CONCURRENT);
  });
  return queue;
}

//+ (BOOL)_isRevocableSessionEnabled {
//  __block BOOL value = NO;
//  dispatch_sync([self _revocableSessionSynchronizationQueue], ^{
//    value = revocableSessionEnabled_;
//  });
//  return value;
//}
//
//+ (void)_setRevocableSessionEnabled:(BOOL)enabled {
//  dispatch_barrier_sync([self _revocableSessionSynchronizationQueue], ^{
//    revocableSessionEnabled_ = enabled;
//  });
//}
//
//+ (BFTask *)_upgradeToRevocableSessionInBackground {
//  BECurrentUserController *controller = [[self class] currentUserController];
//  return [[controller getCurrentUserAsyncWithOptions:0] continueWithSuccessBlock:^id(BFTask *task) {
//    BEUser *currentUser = task.result;
//    NSString *sessionToken = currentUser.sessionToken;
//    
//    // Bail-out early if session token is already revocable.
//    if ([BESessionUtilities isSessionTokenRevocable:sessionToken]) {
//      return currentUser;
//    }
//    return [currentUser _upgradeToRevocableSessionInBackground];
//  }];
//}

//- (BFTask *)_upgradeToRevocableSessionInBackground {
//  @weakify(self);
//  return [self.taskQueue enqueue:^BFTask *(BFTask *toAwait) {
//    return [toAwait continueAsyncWithBlock:^id(BFTask *task) {
//      @strongify(self);
//      
//      NSString *token = nil;
//      @synchronized(self.lock) {
//        token = self.sessionToken;
//      }
//      
//      // Check session token here as well, to make sure we didn't upgrade the token in between.
//      if ([BESessionUtilities isSessionTokenRevocable:token]) {
//        return self;
//      }
//      
//      BERESTCommand *command = [BERESTUserCommand upgradeToRevocableSessionCommandWithSessionToken:token];
//      return [[[CSBM _currentManager].commandRunner runCommandAsync:command
//                                                         withOptions:0] continueWithSuccessBlock:^id(BFTask *task) {
//        NSDictionary *dictionary = [task.result result];
//        BESession *session = [BESession _objectFromDictionary:dictionary
//                                             defaultClassName:[BESession CSBMClassName]
//                                                 completeData:YES];
//        @synchronized(self.lock) {
//          self._state = [self._state copyByMutatingWithBlock:^(BEMutableUserState *state) {
//            state.sessionToken = session.sessionToken;
//          }];
//        }
//        BECurrentUserController *controller = [[self class] currentUserController];
//        return [controller saveCurrentObjectAsync:self];
//      }];
//    }];
//  }];
//}

///--------------------------------------
#pragma mark - Data Source
///--------------------------------------

+ (BEObjectFileCodingLogic *)objectFileCodingLogic {
  return [BEUserFileCodingLogic codingLogic];
}

//+ (BEUserAuthenticationController *)authenticationController {
//  return [CSBM _currentManager].coreManager.userAuthenticationController;
//}
//
//+ (BEUserController *)userController {
//  return [CSBM _currentManager].coreManager.userController;
//}
@end

@implementation BEUser

@dynamic _state;

// BEUser:
@dynamic username;
@dynamic email;
@dynamic password;

// BEUser (Private):
@synthesize authData = _authData;
@synthesize linkedServiceNames = _linkedServiceNames;
@synthesize _current = _current;
@synthesize _lazy = _lazy;

+ (NSString *)CSBMClassName {
  return @"_User";
}

+ (instancetype)currentUser {
  return [[self getCurrentUserInBackground] waitForResult:nil withMainThreadWarning:NO];
}

//+ (BFTask<__kindof BEUser *> *)getCurrentUserInBackground {
//  return [[[self class] currentUserController] getCurrentObjectAsync];
//}

- (BOOL)_current {
  @synchronized(self.lock) {
    return _current;
  }
}

- (void)set_current:(BOOL)current {
  @synchronized(self.lock) {
    _current = current;
  }
}

///--------------------------------------
#pragma mark - Log In
///--------------------------------------

+ (BFTask *)logInWithUsernameInBackground:(NSString *)username password:(NSString *)password {
  return [[self userController] logInCurrentUserAsyncWithUsername:username
                                                         password:password
                                                 revocableSession:[self _isRevocableSessionEnabled]];
}

+ (void)logInWithUsernameInBackground:(NSString *)username
                             password:(NSString *)password
                                block:(BEUserResultBlock)block {
  [[self logInWithUsernameInBackground:username password:password] thenCallBackOnMainThreadAsync:block];
}

///--------------------------------------
#pragma mark - Third-party Authentication
///--------------------------------------

//+ (void)registerAuthenticationDelegate:(id<BEUserAuthenticationDelegate>)delegate forAuthType:(NSString *)authType {
//  [[self authenticationController] registerAuthenticationDelegate:delegate forAuthType:authType];
//}
//
//#pragma mark Log In
//
//+ (BFTask<__kindof BEUser *> *)logInWithAuthTypeInBackground:(NSString *)authType
//                                                    authData:(NSDictionary<NSString *, NSString *> *)authData {
//  BEParameterAssert(authType, @"Can't log in without `authType`.");
//  BEParameterAssert(authData, @"Can't log in without `authData`.");
//  BEUserAuthenticationController *controller = [self authenticationController];
//  BEConsistencyAssert([controller authenticationDelegateForAuthType:authType],
//                      @"No registered authentication delegate found for `%@` authentication type. "
//                      @"Register a delegate first via BEUser.registerAuthenticationDelegate(delegate, forAuthType:)",
//                      authType);
//  return [[self authenticationController] logInUserAsyncWithAuthType:authType authData:authData];
//}
//#pragma mark Link
//
//- (BFTask<NSNumber *> *)linkWithAuthTypeInBackground:(NSString *)authType
//                                            authData:(NSDictionary<NSString *, NSString *> *)newAuthData {
//  BEParameterAssert(authType, @"Can't link without `authType`.");
//  BEParameterAssert(newAuthData, @"Can't link without `authData`.");
//  BEUserAuthenticationController *controller = [[self class] authenticationController];
//  BEConsistencyAssert([controller authenticationDelegateForAuthType:authType],
//                      @"No registered authentication delegate found for `%@` authentication type. "
//                      @"Register a delegate first via BEUser.registerAuthenticationDelegate(delegate, forAuthType:)",
//                      authType);
//  
//  @weakify(self);
//  return [self.taskQueue enqueue:^BFTask *(BFTask *toAwait) {
//    return [toAwait continueWithBlock:^id(BFTask *task) {
//      @strongify(self);
//      
//      NSDictionary *oldAnonymousData = nil;
//      
//      @synchronized (self.lock) {
//        self.authData[authType] = newAuthData;
//        [self.linkedServiceNames addObject:authType];
//        
//        oldAnonymousData = self.authData[BEAnonymousUserAuthenticationType];
//        [self stripAnonymity];
//        
//        dirty = YES;
//      }
//      
//      return [[self saveAsync:nil] continueAsyncWithBlock:^id(BFTask *task) {
//        if (task.result) {
//          [self synchronizeAuthDataWithAuthType:authType];
//          return task;
//        }
//        
//        @synchronized (self.lock) {
//          [self.authData removeObjectForKey:authType];
//          [self.linkedServiceNames removeObject:authType];
//          [self restoreAnonymity:oldAnonymousData];
//        }
//        // Save the user to disk in case of failure, since we want the latest succeeded data persistent.
//        BECurrentUserController *controller = [[self class] currentUserController];
//        return [[controller saveCurrentObjectAsync:self] continueWithBlock:^id(BFTask *_) {
//          return task; // Roll-forward the result of a save to network, not local save.
//        }];
//      }];
//    }];
//  }];
//}
#pragma mark Unlink

- (BFTask *)unlinkWithAuthTypeInBackground:(NSString *)authType {
  return [BFTask taskFromExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:^id {
    @synchronized (self.lock) {
      if (self.authData[authType]) {
        self.authData[authType] = [NSNull null];
        dirty = YES;
        return [self saveInBackground];
      }
    }
    return @YES;
  }];
}

#pragma mark Linked

- (BOOL)isLinkedWithAuthType:(NSString *)authType {
  BEParameterAssert(authType, @"Authentication type can't be `nil`.");
  @synchronized(self.lock) {
    return [self.linkedServiceNames containsObject:authType];
  }
}

#pragma mark Private

//+ (void)_unregisterAuthenticationDelegateForAuthType:(NSString *)authType {
//  [[[self class] authenticationController] unregisterAuthenticationDelegateForAuthType:authType];
//}

///--------------------------------------
#pragma mark - Become
///--------------------------------------

+ (BFTask *)becomeInBackground:(NSString *)sessionToken {
  BEParameterAssert(sessionToken, @"Session Token must be provided for login.");
  return [[self userController] logInCurrentUserAsyncWithSessionToken:sessionToken];
}

+ (void)becomeInBackground:(NSString *)sessionToken block:(BEUserResultBlock)block {
  [[self becomeInBackground:sessionToken] thenCallBackOnMainThreadAsync:block];
}

///--------------------------------------
#pragma mark - Revocable Sessions
///--------------------------------------

//+ (BFTask *)enableRevocableSessionInBackground {
//  if ([self _isRevocableSessionEnabled]) {
//    return [BFTask taskWithResult:nil];
//  }
//  [self _setRevocableSessionEnabled:YES];
//  return [self _upgradeToRevocableSessionInBackground];
//}

+ (void)enableRevocableSessionInBackgroundWithBlock:(BEUserSessionUpgradeResultBlock)block {
  [[self enableRevocableSessionInBackground] continueWithBlock:^id(BFTask *task) {
    block(task.error);
    return nil;
  }];
}

///--------------------------------------
#pragma mark - Request Password Reset
///--------------------------------------

+ (BFTask *)requestPasswordResetForEmailInBackground:(NSString *)email {
  BEParameterAssert(email, @"Email should be provided to request password reset.");
  return [[[self userController] requestPasswordResetAsyncForEmail:email] continueWithSuccessResult:@YES];
}

+ (void)requestPasswordResetForEmailInBackground:(NSString *)email block:(BEBooleanResultBlock)block {
  [[self requestPasswordResetForEmailInBackground:email] thenCallBackOnMainThreadWithBoolValueAsync:block];
}


///--------------------------------------
#pragma mark - Logging out
///--------------------------------------

//+ (BFTask *)logOutInBackground {
//  BECurrentUserController *controller = [[self class] currentUserController];
//  return [controller logOutCurrentUserAsync];
//}

+ (void)logOutInBackgroundWithBlock:(BEUserLogoutResultBlock)block {
  [[self logOutInBackground] continueWithExecutor:[BFExecutor mainThreadExecutor] withBlock:^id(BFTask *task) {
    block(task.error);
    return nil;
  }];
}

//- (BFTask *)_logOutAsync {
//  //TODO: (nlutsenko) Maybe add this to `taskQueue`?
//  
//  NSString *token = nil;
//  NSMutableArray *tasks = [NSMutableArray array];
//  @synchronized(self.lock) {
//    [self.authData enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
//      BFTask *task = [self _logOutAsyncWithAuthType:key];
//      [tasks addObject:task];
//    }];
//    
//    self._current = NO;
//    
//    token = [self.sessionToken copy];
//    
//    self._state = [self._state copyByMutatingWithBlock:^(BEMutableUserState *state) {
//      state.sessionToken = nil;
//    }];
//  }
//  
//  BFTask *task = [BFTask taskForCompletionOfAllTasks:tasks];
//  
//  if ([BESessionUtitlites isSessionTokenRevocable:token]) {
//    return [task continueWithExecutor:[BFExecutor defaultExecutor] withBlock:^id(BFTask *task) {
//      return [[[self class] userController] logOutUserAsyncWithSessionToken:token];
//    }];
//  }
//  return task;
//}

///--------------------------------------
#pragma mark - Accessors
///--------------------------------------

- (void)setObject:(id)object forKey:(NSString *)key {
  BEParameterAssert(_BEUserIsWritablePropertyForKey(key),
                    @"Can't remove the '%@' field of a BEUser.", key);
  if ([key isEqualToString:BEUserUsernameRESTKey]) {
    [self stripAnonymity];
  }
  [super setObject:object forKey:key];
}

- (void)removeObjectForKey:(NSString *)key {
  BEParameterAssert(_BEUserIsRemovablePropertyForKey(key),
                    @"Can't remove the '%@' field of a BEUser.", key);
  [super removeObjectForKey:key];
}

- (NSMutableDictionary *)authData {
  @synchronized([self lock]) {
    if (!_authData) {
      _authData = [[NSMutableDictionary alloc] init];
    }
  }
  return _authData;
}

- (NSMutableSet *)linkedServiceNames {
  @synchronized([self lock]) {
    if (!_linkedServiceNames) {
      _linkedServiceNames = [[NSMutableSet alloc] init];
    }
  }
  return _linkedServiceNames;
}

+ (instancetype)user {
  return [self object];
}

- (BFTask *)saveAsync:(BFTask *)toAwait {
  if (!toAwait) {
    toAwait = [BFTask taskWithResult:nil];
  }
  
  // This breaks a rare deadlock scenario where on one thread, user.lock is acquired before taskQueue.lock sometimes,
  // but not always. Using continueAsyncWithBlock unlocks from the taskQueue, and solves the proplem.
  return [toAwait continueAsyncWithBlock:^id(BFTask *task) {
    @synchronized ([self lock]) {
      if (self._lazy) {
        return [[self resolveLazinessAsync:toAwait] continueAsyncWithSuccessBlock:^id(BFTask *task) {
          return @(!!task.result);
        }];
      }
    }
    
    return [super saveAsync:toAwait];
  }];
}
//- (BFTask *)fetchAsync:(BFTask *)toAwait {
//  if (self._lazy) {
//    return [BFTask taskWithResult:@YES];
//  }
//  
//  return [[super fetchAsync:toAwait] continueAsyncWithSuccessBlock:^id(BFTask *fetchAsyncTask) {
//    if (self._current) {
//      [self cleanUpAuthData];
//      BECurrentUserController *controller = [[self class] currentUserController];
//      return [[controller saveCurrentObjectAsync:self] continueAsyncWithBlock:^id(BFTask *task) {
//        return fetchAsyncTask.result;
//      }];
//    }
//    return fetchAsyncTask.result;
//  }];
//}

- (instancetype)fetch:(NSError **)error {
  if (self._lazy) {
    return self;
  }
  return [super fetch:error];
}

- (void)fetchInBackgroundWithBlock:(BEObjectResultBlock)block {
  if (self._lazy) {
    if (block) {
      block(self, nil);
      return;
    }
  }
  [super fetchInBackgroundWithBlock:^(BEObject *result, NSError *error) {
    if (block) {
      block(result, error);
    }
  }];
}

//- (BFTask *)signUpInBackground {
//  return [self.taskQueue enqueue:^BFTask *(BFTask *toAwait) {
//    return [self signUpAsync:toAwait];
//  }];
//}

- (BOOL)isAuthenticated {
  BEUser *currentUser = [[self class] currentUser];
  return [self _isAuthenticatedWithCurrentUser:currentUser];
}

- (BOOL)_isAuthenticatedWithCurrentUser:(BEUser *)currentUser {
  @synchronized([self lock]) {
    BOOL authenticated = self._lazy || self.sessionToken;
    if (!authenticated && currentUser != nil) {
      authenticated = [self.objectId isEqualToString:currentUser.objectId];
    } else {
      authenticated = self._current;
    }
    return authenticated;
  }
}
- (BOOL)isNew {
  return self._state.isNew;
}

- (NSString *)sessionToken {
  return self._state.sessionToken;
}

- (void)signUpInBackgroundWithBlock:(BEBooleanResultBlock)block {
  @synchronized([self lock]) {
    if (self.objectId) {
      // For anonymous users, there may be an objectId.  Setting the userName
      // will have removed the anonymous link and set the value in the authData
      // object to [NSNull null], so we can just treat it like a save operation.
      if (self.authData[BEAnonymousUserAuthenticationType] == [NSNull null]) {
        [self saveInBackgroundWithBlock:block];
        return;
      }
    }
    [[self signUpInBackground] thenCallBackOnMainThreadWithBoolValueAsync:block];
  }
}

//+ (void)enableAutomaticUser {
//  [CSBM _currentManager].coreManager.currentUserController.automaticUsersEnabled = YES;
//}

///--------------------------------------
#pragma mark - BEObjectPrivateSubclass
///--------------------------------------

#pragma mark State

//+ (BEObjectState *)_newObjectStateWithCSBMClassName:(NSString *)className
//                                            objectId:(NSString *)objectId
//                                          isComplete:(BOOL)complete {
//  return [BEUserState stateWithCSBMClassName:className objectId:objectId isComplete:complete];
//}
@end

@implementation BEUser (Synchronous)

#pragma mark Creating a New User

- (BOOL)signUp {
  return [self signUp:nil];
}

- (BOOL)signUp:(NSError **)error {
  return [[[self signUpInBackground] waitForResult:error] boolValue];
}

#pragma mark Logging In

+ (nullable instancetype)logInWithUsername:(NSString *)username password:(NSString *)password {
  return [self logInWithUsername:username password:password error:nil];
}

+ (nullable instancetype)logInWithUsername:(NSString *)username password:(NSString *)password error:(NSError **)error {
  return [[self logInWithUsernameInBackground:username password:password] waitForResult:error];
}

#pragma mark Becoming a User

+ (nullable instancetype)become:(NSString *)sessionToken {
  return [self become:sessionToken error:nil];
}

+ (nullable instancetype)become:(NSString *)sessionToken error:(NSError **)error {
  return [[self becomeInBackground:sessionToken] waitForResult:error];
}

#pragma mark Logging Out

+ (void)logOut {
  [[self logOutInBackground] waitForResult:nil withMainThreadWarning:NO];
}

#pragma mark Requesting a Password Reset

+ (BOOL)requestPasswordResetForEmail:(NSString *)email {
  return [self requestPasswordResetForEmail:email error:nil];
}

+ (BOOL)requestPasswordResetForEmail:(NSString *)email error:(NSError **)error {
  return [[[self requestPasswordResetForEmailInBackground:email] waitForResult:error] boolValue];
}

@end

///--------------------------------------
#pragma mark - Deprecated
///--------------------------------------

@implementation BEUser (Deprecated)

#pragma mark Creating a new User

- (void)signUpInBackgroundWithTarget:(nullable id)target selector:(nullable SEL)selector {
  [self signUpInBackgroundWithBlock:^(BOOL succeeded, NSError *error) {
    [BEInternalUtils safePerformSelector:selector withTarget:target object:@(succeeded) object:error];
  }];
}

#pragma mark Logging In

+ (void)logInWithUsernameInBackground:(NSString *)username
                             password:(NSString *)password
                               target:(nullable id)target
                             selector:(nullable SEL)selector {
  [self logInWithUsernameInBackground:username password:password block:^(BEUser *user, NSError *error) {
    [BEInternalUtils safePerformSelector:selector withTarget:target object:user object:error];
  }];
}

#pragma mark Becoming a User

+ (void)becomeInBackground:(NSString *)sessionToken target:(nullable id)target selector:(nullable SEL)selector {
  [self becomeInBackground:sessionToken block:^(BEUser *user, NSError *error) {
    [BEInternalUtils safePerformSelector:selector withTarget:target object:user object:error];
  }];
}

#pragma mark Requesting a Password Reset

+ (void)requestPasswordResetForEmailInBackground:(NSString *)email target:(nullable id)target selector:(nullable SEL)selector {
  [self requestPasswordResetForEmailInBackground:email block:^(BOOL succeeded, NSError *error) {
    [BEInternalUtils safePerformSelector:selector withTarget:target object:@(succeeded) object:error];
  }];
}

@end
