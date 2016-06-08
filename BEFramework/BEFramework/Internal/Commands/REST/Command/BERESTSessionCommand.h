//
//  BERESTSessionCommand.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BERESTCommand.h"

@interface BERESTSessionCommand : BERESTCommand

+ (instancetype)getCurrentSessionCommandWithSessionToken:(nullable NSString *)sessionToken;

@end
