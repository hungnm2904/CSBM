//
//  BEObjectEstimateData.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

@class BEFieldOperation;
@class BEOperationSet;

@interface BEObjectEstimatedData : NSObject

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithServerData:(NSDictionary *)serverData
                 operationSetQueue:(NSArray *)operationSetQueue;
+ (instancetype)estimatedDataFromServerData:(NSDictionary *)serverData
                          operationSetQueue:(NSArray *)operationSetQueue;

///--------------------------------------
#pragma mark - Read
///--------------------------------------

- (id)objectForKey:(NSString *)key;
- (id)objectForKeyedSubscript:(NSString *)keyedSubscript;

- (void)enumerateKeysAndObjectsUsingBlock:(void (^)(NSString *key, id obj, BOOL *stop))block;

@property (nonatomic, copy, readonly) NSArray *allKeys;
@property (nonatomic, copy, readonly) NSDictionary *dictionaryRepresentation;

///--------------------------------------
#pragma mark - Write
///--------------------------------------

- (id)applyFieldOperation:(BEFieldOperation *)operation forKey:(NSString *)key;

@end
