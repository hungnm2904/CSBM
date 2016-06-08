//
//  BERESTSessionCommand.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BERESTSessionCommand.h"
#import "BEHTTPRequest.h"

@implementation BERESTSessionCommand

+ (instancetype)getCurrentSessionCommandWithSessionToken:(nullable NSString *)sessionToken {
  return [self commandWithHTTPPath:@"sessions/me"
                        httpMethod:BEHTTPRequestMethodGET
                        parameters:nil
                      sessionToken:sessionToken];
}

@end
