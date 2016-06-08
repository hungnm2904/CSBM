//
//  BEURLConstructor.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEURLConstructor.h"
#import "BEAssert.h"
@implementation BEURLConstructor

///--------------------------------------
#pragma mark - Basic
///--------------------------------------

+ (NSURL *)URLFromAbsoluteString:(NSString *)string
                            path:(nullable NSString *)path
                           query:(nullable NSString *)query {
  NSURLComponents *components = [NSURLComponents componentsWithString:string];
  if (path.length != 0) {
    NSString *fullPath = (components.path.length ? components.path : @"/");
    fullPath = [fullPath stringByAppendingPathComponent:path];
    // If the last character in the provided path is a `/` -> `stringByAppendingPathComponent:` will remove it.
    // so we need to append it manually to make sure we contruct with the requested behavior.
    if ([path characterAtIndex:path.length - 1] == '/' &&
        [fullPath characterAtIndex:fullPath.length - 1] != '/') {
      fullPath = [fullPath stringByAppendingString:@"/"];
    }
    components.path = fullPath;
  }
  if (query) {
    components.query = query;
  }
  return components.URL;
}

@end
