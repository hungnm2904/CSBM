//
//  BEQueryUtilities.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface BEQueryUtilities : NSObject

///--------------------------------------
#pragma mark - Predicate
///--------------------------------------

/**
 Takes an arbitrary predicate and normalizes it to a form that can easily be converted to a `BEQuery`.
 */
+ (NSPredicate *)predicateByNormalizingPredicate:(NSPredicate *)predicate;

///--------------------------------------
#pragma mark - Regex
///--------------------------------------

/**
 Converts a string into a regex that matches it.
 
 @param string String to convert from.
 
 @return Query regex string from a string.
 */
+ (NSString *)regexStringForString:(NSString *)string;

///--------------------------------------
#pragma mark - Errors
///--------------------------------------

+ (NSError *)objectNotFoundError;

@end
