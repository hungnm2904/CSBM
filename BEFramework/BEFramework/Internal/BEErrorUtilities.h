//
//  BEErrorUtilities.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface BEErrorUtilities : NSObject

/**
 Construct an error object from a code and a message.
 
 @description Note that this logs all errors given to it.
 You should use `errorWithCode:message:shouldLog:` to explicitly control whether it logs.
 
 @param code    Parse Error Code
 @param message Error description
 
 @return `NSError` instance.
 */
+ (NSError *)errorWithCode:(NSInteger)code message:(NSString *)message;
+ (NSError *)errorWithCode:(NSInteger)code message:(NSString *)message shouldLog:(BOOL)shouldLog;

/**
 Construct an error object from a result dictionary the API returned.
 
 @description Note that this logs all errors given to it.
 You should use `errorFromResult:shouldLog:` to explicitly control whether it logs.
 
 @param result Network command result.
 
 @return `NSError` instance.
 */
+ (NSError *)errorFromResult:(NSDictionary *)result;
+ (NSError *)errorFromResult:(NSDictionary *)result shouldLog:(BOOL)shouldLog;

@end
