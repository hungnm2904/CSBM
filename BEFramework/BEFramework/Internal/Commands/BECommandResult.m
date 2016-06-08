//
//  BECommandResult.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BECommandResult.h"
#import "BEAssert.h"

@implementation BECommandResult

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithResult:(NSDictionary *)result
                  resultString:(NSString *)resultString
                  httpResponse:(NSHTTPURLResponse *)response {
  self = [super init];
  if (!self) return nil;
  
  _result = result;
  _resultString = [resultString copy];
  _httpResponse = response;
  
  return self;
}

+ (instancetype)commandResultWithResult:(NSDictionary *)result
                           resultString:(NSString *)resultString
                           httpResponse:(NSHTTPURLResponse *)response {
  return [[self alloc] initWithResult:result resultString:resultString httpResponse:response];
}


@end
