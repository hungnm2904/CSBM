//
//  CSBMInternal.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "CSBM.h"
#import "BEAssert.h"
#import "BEEventuallyQueue.h"
#import "BEInternalUtils.h"
#import "BEObjectPrivate.h"
#import "BEUserPrivate.h"
#import "CSBMModule.h"

@interface CSBM (CSBMModules)

+ (void)enableParseModule:(id<CSBMModule>)module;
+ (void)disableParseModule:(id<CSBMModule>)module;
+ (BOOL)isModuleEnabled:(id<CSBMModule>)module;

@end
