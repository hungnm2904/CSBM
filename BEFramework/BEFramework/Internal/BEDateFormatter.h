//
//  BEDateFormatter.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BEDateFormatter : NSObject

+ (instancetype)sharedFormatter;

///--------------------------------------
#pragma mark - String from Date
///--------------------------------------

/**
 Converts `NSDate` into `NSString` representation using the following format: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
 
 @param date `NSDate` to convert.
 
 @return Formatted `NSString` representation.
 */
- (NSString *)preciseStringFromDate:(NSDate *)date;

///--------------------------------------
#pragma mark - Date from String
///--------------------------------------

/**
 Converts `NSString` representation of a date into `NSDate` object.
 
 Following date formats are supported:
 YYYY-MM-DD
 YYYY-MM-DD HH:MM'Z'
 YYYY-MM-DD HH:MM:SS'Z'
 YYYY-MM-DD HH:MM:SS.SSS'Z'
 YYYY-MM-DDTHH:MM'Z'
 YYYY-MM-DDTHH:MM:SS'Z'
 YYYY-MM-DDTHH:MM:SS.SSS'Z'
 
 @param string `NSString` representation to convert.
 
 @return `NSDate` incapsulating the date.
 */
- (NSDate *)dateFromString:(NSString *)string;

@end

NS_ASSUME_NONNULL_END