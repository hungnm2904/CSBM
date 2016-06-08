//
//  BEJSONSerialization.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface BEJSONSerialization : NSObject

/**
 The object passed in must be one of:
 * NSString
 * NSNumber
 * NSDictionary
 * NSArray
 * NSNull
 
 @return NSData of JSON representing the passed in object.
 */
+ (nullable NSData *)dataFromJSONObject:(id)object;

/**
 The object passed in must be one of:
 * NSString
 * NSNumber
 * NSDictionary
 * NSArray
 * NSNull
 
 @return NSString of JSON representing the passed in object.
 */
+ (nullable NSString *)stringFromJSONObject:(id)object;

/**
 Takes a JSON string and returns the NSDictionaries and NSArrays in it.
 You should still call decodeObject if you want Parse types.
 */
+ (nullable id)JSONObjectFromData:(NSData *)data;

/**
 Takes a JSON string and returns the NSDictionaries and NSArrays in it.
 You should still call decodeObject if you want Parse types.
 */
+ (nullable id)JSONObjectFromString:(NSString *)string;

/**
 Takes a file path to json file and returns the NSDictionaries and NSArrays in it.
 
 @description You should still call decodeObject if you want Parse types.
 
 @param filePath File path to a file.
 
 @return Decoded object.
 */
+ (nullable id)JSONObjectFromFileAtPath:(NSString *)filePath;

@end
