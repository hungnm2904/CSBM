//
//  BERESTConfigCommand.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BERESTCommand.h"

@interface BERESTConfigCommand : BERESTCommand

+ (instancetype)configFetchCommandWithSessionToken:(nullable NSString *)sessionToken;
+ (instancetype)configUpdateCommandWithConfigParameters:(NSDictionary *)parameters
                                           sessionToken:(nullable NSString *)sessionToken;

@end
