//
//  BEURLConstructor.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface BEURLConstructor : NSObject

+ (NSURL *)URLFromAbsoluteString:(NSString *)string
                            path:(nullable NSString *)path
                           query:(nullable NSString *)query;

@end
