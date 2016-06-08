//
//  BEBase64Encoder.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface BEBase64Encoder : NSObject

+ (NSData *)dataFromBase64String:(NSString *)string;
+ (NSString *)base64StringFromData:(NSData *)data;

@end
