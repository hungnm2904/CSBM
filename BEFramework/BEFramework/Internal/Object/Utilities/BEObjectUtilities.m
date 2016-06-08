//
//  BEObjectUtilities.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEObjectUtilities.h"
#import "BEFieldOperation.h"
#import "BEOperationSet.h"

@implementation BEObjectUtilities

///--------------------------------------
#pragma mark - Operations
///--------------------------------------

+ (id)newValueByApplyingFieldOperation:(BEFieldOperation *)operation
                          toDictionary:(NSMutableDictionary *)dictionary
                                forKey:(NSString *)key {
  id oldValue = dictionary[key];
  id newValue = [operation applyToValue:oldValue forKey:key];
  if (newValue) {
    dictionary[key] = newValue;
  } else {
    [dictionary removeObjectForKey:key];
  }
  return newValue;
}

+ (void)applyOperationSet:(BEOperationSet *)operationSet toDictionary:(NSMutableDictionary *)dictionary {
//  [operationSet enumerateKeysAndObjectsUsingBlock:^(NSString *key, BEFieldOperation *obj, BOOL *stop) {
//    [self newValueByApplyingFieldOperation:obj toDictionary:dictionary forKey:key];
//  }];
}

///--------------------------------------
#pragma mark - Equality
///--------------------------------------

+ (BOOL)isObject:(id<NSObject>)objectA equalToObject:(id<NSObject>)objectB {
  return (objectA == objectB || (objectA != nil && [objectA isEqual:objectB]));
}

@end
