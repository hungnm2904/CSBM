//
//  BEConfig_Private.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEConfig.h"

extern NSString *const BEConfigParametersRESTKey;

@interface BEConfig (Private)

@property (atomic, copy, readonly) NSDictionary *parametersDictionary;

- (instancetype)initWithFetchedConfig:(NSDictionary *)config;

@end
