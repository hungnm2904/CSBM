//
//  BEErrorUtilities.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEErrorUtilities.h"
#import "BEConstants.h"
#import "BELogging.h"

@implementation BEErrorUtilities

+ (NSError *)errorWithCode:(NSInteger)code message:(NSString *)message {
  return [self errorWithCode:code message:message shouldLog:YES];
}

+ (NSError *)errorWithCode:(NSInteger)code message:(NSString *)message shouldLog:(BOOL)shouldLog {
  NSDictionary *result = @{ @"code" : @(code),
                            @"error" : message };
  return [self errorFromResult:result shouldLog:shouldLog];
}

+ (NSError *)errorFromResult:(NSDictionary *)result {
  return [self errorFromResult:result shouldLog:YES];
}

+ (NSError *)errorFromResult:(NSDictionary *)result shouldLog:(BOOL)shouldLog {
  NSInteger errorCode = [result[@"code"] integerValue];
  
  NSString *errorExplanation = result[@"error"];
  
//  if (shouldLog) {
//    BELogError(BELoggingTagCommon,
//               @"%@ (Code: %ld, Version: %@)", errorExplanation, (long)errorCode, PARSE_VERSION);
//  }
  
  NSMutableDictionary *userInfo = [NSMutableDictionary dictionaryWithDictionary:result];
  if (errorExplanation) {
    userInfo[NSLocalizedDescriptionKey] = errorExplanation;
  }
  return [NSError errorWithDomain:BEParseErrorDomain code:errorCode userInfo:userInfo];
}

@end
