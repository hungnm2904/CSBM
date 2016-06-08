//
//  BERESTConfigCommand.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BERESTConfigCommand.h"
#import "BEAssert.h"
#import "BEHTTPRequest.h"

@implementation BERESTConfigCommand

+ (instancetype)configFetchCommandWithSessionToken:(NSString *)sessionToken {
  return [self commandWithHTTPPath:@"config"
                        httpMethod:BEHTTPRequestMethodGET
                        parameters:nil
                      sessionToken:sessionToken];
}

+ (instancetype)configUpdateCommandWithConfigParameters:(NSDictionary *)parameters
                                           sessionToken:(NSString *)sessionToken {
  NSDictionary *commandParameters = @{ @"params" : parameters };
  return [self commandWithHTTPPath:@"config"
                        httpMethod:BEHTTPRequestMethodPUT
                        parameters:commandParameters
                      sessionToken:sessionToken];
}

@end
