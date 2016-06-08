//
//  BEBase64Encoder.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEBase64Encoder.h"

@implementation BEBase64Encoder

+ (NSData *)dataFromBase64String:(NSString *)string {
  if (!string) {
    return [NSData data];
  }
  return [[NSData alloc] initWithBase64EncodedString:string options:NSDataBase64DecodingIgnoreUnknownCharacters];
}

+ (NSString *)base64StringFromData:(NSData *)data {
  if (!data) {
    return [NSString string];
  }
  return [data base64EncodedStringWithOptions:0];
}

@end
