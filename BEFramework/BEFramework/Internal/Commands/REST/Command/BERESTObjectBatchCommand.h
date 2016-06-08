//
//  BERESTObjectBatchCommand.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BERESTCommand.h"
#import "BEConstants.h"

extern NSUInteger const BERESTObjectBatchCommandSubcommandsLimit;


@interface BERESTObjectBatchCommand : BERESTCommand

+ (instancetype)batchCommandWithCommands:(NSArray<BERESTCommand *> *)commands
                            sessionToken:(nullable NSString *)sessionToken
                               serverURL:(NSURL *)serverURL;

@end
