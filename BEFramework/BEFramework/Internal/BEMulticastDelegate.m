//
//  BEMulticastDelegate.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEMulticastDelegate.h"

@interface BEMulticastDelegate () {
  NSMutableArray *_callbacks;
}

@end

@implementation BEMulticastDelegate
- (instancetype)init {
  self = [super init];
  if (!self) return nil;
  
  _callbacks = [[NSMutableArray alloc] init];
  
  return self;
}

- (void)subscribe:(void(^)(id result, NSError *error))block {
  [_callbacks addObject:block];
}

- (void)unsubscribe:(void(^)(id result, NSError *error))block {
  [_callbacks removeObject:block];
}

- (void)invoke:(id)result error:(NSError *)error {
  NSArray *callbackCopy = [_callbacks copy];
  for (void (^block)(id result, NSError *error) in callbackCopy) {
    block(result, error);
  }
}
- (void)clear {
  [_callbacks removeAllObjects];
}
@end
