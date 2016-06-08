//
//  BEMulticastDelegate.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface BEMulticastDelegate : NSObject
- (void)subscribe:(void(^)(id result, NSError *error))block;
- (void)unsubscribe:(void(^)(id result, NSError *error))block;
- (void)invoke:(id)result error:(NSError *)error;
- (void)clear;
@end
