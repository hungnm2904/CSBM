//
//  BESessionUtitlites.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BESessionUtitlites.h"

@implementation BESessionUtitlites

+ (BOOL)isSessionTokenRevocable:(NSString *)sessionToken {
  return (sessionToken && [sessionToken rangeOfString:@"r:"].location != NSNotFound);
}

@end
