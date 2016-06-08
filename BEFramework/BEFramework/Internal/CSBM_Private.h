//
//  CSBM_Private.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CSBM.h"
#import "BEManager.h"

@interface CSBM_Private : NSObject
+ (void)_resetDataSharingIdentifier;
+ (BEManager *)_currentManager;
+ (void)_clearCurrentManager;
@end
