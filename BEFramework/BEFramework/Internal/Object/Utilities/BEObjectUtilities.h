//
//  BEObjectUtilities.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

@class BEFieldOperation;
@class BEOperationSet;

@interface BEObjectUtilities : NSObject

#pragma mark - Operations

+ (id)newValueByApplyingFieldOperation:(BEFieldOperation *)operation
                          toDictionary:(NSMutableDictionary *)dictionary
                                forKey:(NSString *)key;
+ (void)applyOperationSet:(BEOperationSet *)operationSet toDictionary:(NSMutableDictionary *)dictionary;

///--------------------------------------
#pragma mark - Equality
///--------------------------------------

+ (BOOL)isObject:(nullable id<NSObject>)objectA equalToObject:(nullable id<NSObject>)objectB;

@end
