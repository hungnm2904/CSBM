//
//  BERESTObjectCommand.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BERESTObjectCommand.h"
#import "BEAssert.h"
#import "BEHTTPRequest.h"
#import "BEObjectState.h"

@implementation BERESTObjectCommand

+ (instancetype)fetchObjectCommandForObjectState:(BEObjectState *)state
                                withSessionToken:(NSString *)sessionToken {
  BEParameterAssert(state.objectId.length, @"objectId should be non nil");
  BEParameterAssert(state.csbmClassName.length, @"Class name should be non nil");
  
  NSString *httpPath = [NSString stringWithFormat:@"classes/%@/%@", state.csbmClassName, state.objectId];
  BERESTObjectCommand *command = [self commandWithHTTPPath:httpPath
                                                httpMethod:BEHTTPRequestMethodGET
                                                parameters:nil
                                              sessionToken:sessionToken];
  return command;
}

+ (instancetype)createObjectCommandForObjectState:(BEObjectState *)state
                                          changes:(NSDictionary *)changes
                                 operationSetUUID:(NSString *)operationSetIdentifier
                                     sessionToken:(NSString *)sessionToken {
  BEParameterAssert(state.csbmClassName.length, @"Class name should be non nil");
  
  NSString *httpPath = [NSString stringWithFormat:@"classes/%@", state.csbmClassName];
  BERESTObjectCommand *command = [self commandWithHTTPPath:httpPath
                                                httpMethod:BEHTTPRequestMethodPOST
                                                parameters:changes
                                          operationSetUUID:operationSetIdentifier
                                              sessionToken:sessionToken];
  return command;
}

+ (instancetype)updateObjectCommandForObjectState:(BEObjectState *)state
                                          changes:(NSDictionary *)changes
                                 operationSetUUID:(NSString *)operationSetIdentifier
                                     sessionToken:(NSString *)sessionToken {
  BEParameterAssert(state.csbmClassName.length, @"Class name should be non nil");
  BEParameterAssert(state.objectId.length, @"objectId should be non nil");
  
  NSString *httpPath = [NSString stringWithFormat:@"classes/%@/%@", state.csbmClassName, state.objectId];
  BERESTObjectCommand *command = [self commandWithHTTPPath:httpPath
                                                httpMethod:BEHTTPRequestMethodPUT
                                                parameters:changes
                                          operationSetUUID:operationSetIdentifier
                                              sessionToken:sessionToken];
  return command;
}

+ (instancetype)deleteObjectCommandForObjectState:(BEObjectState *)state
                                 withSessionToken:(NSString *)sessionToken {
  BEParameterAssert(state.csbmClassName.length, @"Class name should be non nil");
  
  NSMutableString *httpPath = [NSMutableString stringWithFormat:@"classes/%@", state.csbmClassName];
  if (state.objectId) {
    [httpPath appendFormat:@"/%@", state.objectId];
  }
  BERESTObjectCommand *command = [self commandWithHTTPPath:httpPath
                                                httpMethod:BEHTTPRequestMethodDELETE
                                                parameters:nil
                                              sessionToken:sessionToken];
  return command;
}

@end