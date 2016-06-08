//
//  BEObjectEstimateData.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEObjectEstimateData.h"
#import "BEObjectUtilities.h"

@interface BEObjectEstimatedData () {
  NSMutableDictionary *_dataDictionary;
}

@end

@implementation BEObjectEstimatedData

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init {
  self = [super init];
  if (!self) return nil;
  
  _dataDictionary = [NSMutableDictionary dictionary];
  
  return self;
}

- (instancetype)initWithServerData:(NSDictionary *)serverData
                 operationSetQueue:(NSArray *)operationSetQueue {
  self = [super init];
  if (!self) return nil;
  
  // Don't use mutableCopy to make sure we never initialize _dataDictionary to `nil`.
  _dataDictionary = [NSMutableDictionary dictionaryWithDictionary:serverData];
  for (BEOperationSet *operationSet in operationSetQueue) {
    [BEObjectUtilities applyOperationSet:operationSet toDictionary:_dataDictionary];
  }
  
  return self;
}

+ (instancetype)estimatedDataFromServerData:(NSDictionary *)serverData
                          operationSetQueue:(NSArray *)operationSetQueue {
  return [[self alloc] initWithServerData:serverData operationSetQueue:operationSetQueue];
}

///--------------------------------------
#pragma mark - Read
///--------------------------------------

- (void)enumerateKeysAndObjectsUsingBlock:(void (^)(NSString *key, id obj, BOOL *stop))block {
  [_dataDictionary enumerateKeysAndObjectsUsingBlock:block];
}

- (id)objectForKey:(NSString *)key {
  return [_dataDictionary objectForKey:key];
}

- (id)objectForKeyedSubscript:(NSString *)keyedSubscript {
  return [_dataDictionary objectForKeyedSubscript:keyedSubscript];
}

- (NSArray *)allKeys {
  return _dataDictionary.allKeys;
}

- (NSDictionary *)dictionaryRepresentation {
  return [_dataDictionary copy];
}

///--------------------------------------
#pragma mark - Write
///--------------------------------------

- (id)applyFieldOperation:(BEFieldOperation *)operation forKey:(NSString *)key {
  return [BEObjectUtilities newValueByApplyingFieldOperation:operation toDictionary:_dataDictionary forKey:key];
}

@end
