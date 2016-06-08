//
//  BESession.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BESession.h"
#import "BFTask+Private.h"
#import "BEAssert.h"
#import "BECoreManager.h"
#import "BECurrentUserController.h"
#import "BEObject+Subclass.h"
#import "BEObjectPrivate.h"
#import "BESessionController.h"
#import "BEUserPrivate.h"
#import "CSBM_Private.h"

static BOOL _BESessionIsWritablePropertyForKey(NSString *key) {
  static NSSet *protectedKeys;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    protectedKeys = [NSSet setWithObjects:
                     @"sessionToken",
                     @"restricted",
                     @"createdWith",
                     @"installationId",
                     @"user",
                     @"expiresAt", nil];
  });
  return ![protectedKeys containsObject:key];
}

@implementation BESession

@dynamic sessionToken;

///--------------------------------------
#pragma mark - BESubclassing
///--------------------------------------

+ (NSString *)parseClassName {
  return @"_Session";
}

- (BOOL)needsDefaultACL {
  return NO;
}

///--------------------------------------
#pragma mark - Class
///--------------------------------------

+ (void)_assertValidInstanceClassName:(NSString *)className {
  BEParameterAssert([className isEqualToString:[BESession parseClassName]],
                    @"Cannot initialize a BESession with a custom class name.");
}

#pragma mark Get Current Session

//+ (BFTask *)getCurrentSessionInBackground {
//  BECurrentUserController *controller = [[self class] currentUserController];
//  return [[controller getCurrentUserSessionTokenAsync] continueWithBlock:^id(BFTask *task) {
//    NSString *sessionToken = task.result;
//    return [[self sessionController] getCurrentSessionAsyncWithSessionToken:sessionToken];
//  }];
//}

+ (void)getCurrentSessionInBackgroundWithBlock:(BESessionResultBlock)block {
  [[self getCurrentSessionInBackground] thenCallBackOnMainThreadAsync:block];
}

///--------------------------------------
#pragma mark - Accessors
///--------------------------------------

- (void)setObject:(id)object forKey:(NSString *)key {
  BEParameterAssert(_BESessionIsWritablePropertyForKey(key),
                    @"Can't change the '%@' field of a BESession.", key);
  [super setObject:object forKey:key];
}

- (void)removeObjectForKey:(NSString *)key {
  BEParameterAssert(_BESessionIsWritablePropertyForKey(key),
                    @"Can't remove the '%@' field of a BESession.", key);
  [super removeObjectForKey:key];
}

- (void)removeObjectsInArray:(NSArray *)objects forKey:(NSString *)key {
  BEParameterAssert(_BESessionIsWritablePropertyForKey(key),
                    @"Can't remove any object from '%@' field of a BESession.", key);
  [super removeObjectsInArray:objects forKey:key];
}

///--------------------------------------
#pragma mark - Session Controller
///--------------------------------------

//+ (BESessionController *)sessionController {
//  return [CSBM _currentManager].coreManager.sessionController;
//}

@end
