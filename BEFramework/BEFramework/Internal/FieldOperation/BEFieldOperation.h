//
//  BEFieldOperation.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "BEEncoder.h"

@class BEDecoder;
@class BEObject;

@interface BEFieldOperation : NSObject

- (id)encodeWithObjectEncoder:(BEEncoder *)objectEncoder;

- (BEFieldOperation *)mergeWithPrevious:(BEFieldOperation *)previous;

- (id)applyToValue:(id)oldValue forKey:(NSString *)key;

@end

#pragma mark - Independent Operations

@interface BESetOperation : BEFieldOperation

@property (nonatomic, strong, readonly) id value;

- (instancetype)init NS_UNAVAILABLE;
- (instancetype)initWithValue:(id)value NS_DESIGNATED_INITIALIZER;
+ (instancetype)setWithValue:(id)newvalue;

@end

@interface BEDeleteOperation : BEFieldOperation

+ (instancetype)operation;

@end

@interface BEIncrementOperation : BEFieldOperation

@property (nonatomic, strong, readonly) NSNumber *amount;

- (instancetype)init NS_UNAVAILABLE;
- (instancetype)initWithAmount:(NSNumber *)amount NS_DESIGNATED_INITIALIZER;
+ (instancetype)incrementWithAmount:(NSNumber *)amount;

@end

#pragma mark - Array Operations

@interface BEAddOperation : BEFieldOperation

@property (nonatomic, strong, readonly) NSArray *objects;

+ (instancetype)addWithObjects:(NSArray *)array;

@end

@interface BEAddUniqueOperation : BEFieldOperation

@property (nonatomic, strong, readonly) NSArray *objects;

+ (instancetype)addUniqueWithObjects:(NSArray *)array;

@end

@interface BERemoveOperation : BEFieldOperation

@property (nonatomic, strong, readonly) NSArray *objects;

+ (instancetype)removeWithObjects:(NSArray *)array;

@end

@interface BERelationOperation : BEFieldOperation

@property (nonatomic, copy) NSString *targetClass;
@property (nonatomic, strong) NSMutableSet *relationsToAdd;
@property (nonatomic, strong) NSMutableSet *relationsToRemove;

+ (instancetype)addRelationToObjects:(NSArray *)targets;
+ (instancetype)removeRelationToObjects:(NSArray *)targets;

@end