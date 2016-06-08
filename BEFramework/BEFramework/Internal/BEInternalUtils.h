//
//  BEInternalUtils.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEConstants.h"
#import "BEEncoder.h"

@class BENetworkCommand;

@interface BEInternalUtils : NSObject

+ (NSString *)csbmServerURLString;
+ (void)setCSBMServer:(NSString *)server;

+ (NSString *)currentSystemTimeZoneName;

/**
 * Performs selector on the target, only if the target and selector are non-nil,
 * as well as target responds to selector
 */
+ (void)safePerformSelector:(SEL)selector withTarget:(id)target object:(id)object object:(id)anotherObject;

+ (NSNumber *)addNumber:(NSNumber *)first withNumber:(NSNumber *)second;

//
// Given an NSDictionary/NSArray/NSNumber/NSString even nested ones
// Generates a cache key that can be used to identify this object
+ (NSString *)cacheKeyForObject:(id)object;

/**!
 * Does a deep traversal of every item in object, calling block on every one.
 * @param object The object or array to traverse deeply.
 * @param block The block to call for every item. It will be passed the item
 * as an argument. If it returns a truthy value, that value will replace the
 * item in its parent container.
 * @return The result of calling block on the top-level object itself.
 **/
+ (id)traverseObject:(id)object usingBlock:(id (^)(id object))block;

/**
 This method will split an array into multiple arrays, each with up to maximum components count.
 
 @param array      Array to split.
 @param components Number of components that should be used as a max per each subarray.
 
 @return Array of arrays constructed by splitting the array.
 */
+ (NSArray *)arrayBySplittingArray:(NSArray *)array withMaximumComponentsPerSegment:(NSUInteger)components;

+ (id)_stringWithFormat:(NSString *)format arguments:(NSArray *)arguments;
@end
