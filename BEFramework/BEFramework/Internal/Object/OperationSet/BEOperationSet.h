//
//  BEOperationSet.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

@class BEDecoder;
@class BEEncoder;
@class BEFieldOperation;

@interface BEOperationSet : NSObject <NSCopying, NSFastEnumeration>

@property (nonatomic, assign, getter=isSaveEventually) BOOL saveEventually;

@property (nonatomic, copy, readonly) NSString *uuid;

@property (nonatomic, copy) NSDate *updatedAt;

- (void)mergeOperationSet:(BEOperationSet *)other;

- (NSDictionary *)RESTDictionaryUsingObjectEncoder:(BEEncoder *)objectEncoder
                                  operationSetUUID:(NSArray **)operatioSETUUIDs;

+ (BEOperationSet *)operationSetFromRESTDictionary:(NSDictionary *)data
   usingDecoder:(BEDecoder *)decoder;
#pragma mark - Accessors

@property (nonatomic, assign, readonly) NSUInteger count;
- (id)objectForKey:(id)aKey;
- (id)objectForKeyedSubscript:(id)aKey;
- (NSEnumerator *)keyEnumerator;

- (void)setObject:(id)anObject forKey:(id<NSCopying>)aKey;
- (void)setObject:(id)anObject forKeySubscript:(id<NSCopying>)aKey;
- (void)removeObjectForKey:(id)aKey;
- (void)removeAllObjects;
@end
