//
//  BERESTObjectBatchCommand.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BERESTObjectBatchCommand.h"
#import "BEAssert.h"
#import "BEHTTPRequest.h"
#import "BEURLConstructor.h"

NSUInteger const BERESTObjectBatchCommandSubcommandsLimit = 50;

@implementation BERESTObjectBatchCommand

+ (nonnull instancetype)batchCommandWithCommands:(nonnull NSArray<BERESTCommand *> *)commands
                                    sessionToken:(nullable NSString *)sessionToken
                                       serverURL:(nonnull NSURL *)serverURL {
  BEParameterAssert(commands.count <= BERESTObjectBatchCommandSubcommandsLimit,
                    @"Max of %d commands are allowed in a single batch command",
                    (int)BERESTObjectBatchCommandSubcommandsLimit);
  
  NSMutableArray *requests = [NSMutableArray arrayWithCapacity:commands.count];
  for (BERESTCommand *command in commands) {
    NSURL *commandURL = [BEURLConstructor URLFromAbsoluteString:serverURL.absoluteString
                                                           path:command.httpPath
                                                          query:nil];
    NSMutableDictionary *requestDictionary = [@{ @"method" : command.httpMethod,
                                                 @"path" : commandURL.path } mutableCopy];
    if (command.parameters) {
      requestDictionary[@"body"] = command.parameters;
    }
    
    [requests addObject:requestDictionary];
  }
  return [self commandWithHTTPPath:@"batch"
                        httpMethod:BEHTTPRequestMethodPOST
                        parameters:@{ @"requests" : requests }
                      sessionToken:sessionToken];
}

@end