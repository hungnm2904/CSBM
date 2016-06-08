//
//  BEUserFileCodingLogic.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEUserFileCodingLogic.h"
#import "BEDecoder.h"
#import "BEMutableUserState.h"
#import "BEObjectPrivate.h"
#import "BEUserConstants.h"
#import "BEUserPrivate.h"

@interface BEUserFileCodingLogic()

@end

@implementation BEUserFileCodingLogic

///--------------------------------------
#pragma mark - Coding
///--------------------------------------

- (void)updateObject:(BEObject *)object fromDictionary:(NSDictionary *)dictionary usingDecoder:(BEDecoder *)decoder {
  BEUser *user = (BEUser *)object;
  
  NSString *newSessionToken = dictionary[@"session_token"] ?: dictionary[BEUserSessionTokenRESTKey];
  if (newSessionToken) {
    user._state = [(BEUserState *)user._state copyByMutatingWithBlock:^(BEMutableUserState *state) {
      state.sessionToken = newSessionToken;
    }];
  }
  
  // Merge the linked service metadata
  NSDictionary *newAuthData = dictionary[@"auth_data"] ?: dictionary[BEUserAuthDataRESTKey];
  newAuthData = [decoder decodeObject:newAuthData];
  if (newAuthData) {
    [user.authData removeAllObjects];
    [user.linkedServiceNames removeAllObjects];
    [newAuthData enumerateKeysAndObjectsUsingBlock:^(id key, id linkData, BOOL *stop) {
      if (linkData != [NSNull null]) {
        user.authData[key] = linkData;
        [user.linkedServiceNames addObject:key];
        [user synchronizeAuthDataWithAuthType:key];
      } else {
        [user.authData removeObjectForKey:key];
        [user.linkedServiceNames removeObject:key];
        [user synchronizeAuthDataWithAuthType:key];
      }
    }];
  }
  
  [super updateObject:user fromDictionary:dictionary usingDecoder:decoder];
}

@end
