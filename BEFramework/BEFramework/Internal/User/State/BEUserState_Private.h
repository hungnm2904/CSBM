//
//  BEUserState_Private.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEUserState.h"

@interface BEUserState() {
@protected
  NSString *_sessionToken;
  NSDictionary *_authData;
  
  BOOL _isNew;
}

@property (nonatomic, copy, readwrite) NSString *sessionToken;
@property (nonatomic, copy, readwrite) NSDictionary *authData;

@property (nonatomic, assign, readwrite) BOOL isNew;
@end
