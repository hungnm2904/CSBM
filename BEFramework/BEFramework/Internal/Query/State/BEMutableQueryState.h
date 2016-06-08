//
//  BEMutableQueryState.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEQueryState.h"

@interface BEMutableQueryState : BEQueryState <NSCopying>

@property (nonatomic, copy, readwrite) NSString *parseClassName;

@property (nonatomic, assign, readwrite) NSInteger limit;
@property (nonatomic, assign, readwrite) NSInteger skip;

///--------------------------------------
#pragma mark - Remote + Caching Options
///--------------------------------------

@property (nonatomic, assign, readwrite) BECachePolicy cachePolicy;
@property (nonatomic, assign, readwrite) NSTimeInterval maxCacheAge;

@property (nonatomic, assign, readwrite) BOOL trace;

///--------------------------------------
#pragma mark - Local Datastore Options
///--------------------------------------

@property (nonatomic, assign, readwrite) BOOL shouldIgnoreACLs;
@property (nonatomic, assign, readwrite) BOOL shouldIncludeDeletingEventually;
@property (nonatomic, assign, readwrite) BOOL queriesLocalDatastore;
@property (nonatomic, copy, readwrite) NSString *localDatastorePinName;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithParseClassName:(NSString *)className;
+ (instancetype)stateWithParseClassName:(NSString *)className;

///--------------------------------------
#pragma mark - Conditions
///--------------------------------------

- (void)setConditionType:(NSString *)type withObject:(id)object forKey:(NSString *)key;

- (void)setEqualityConditionWithObject:(id)object forKey:(NSString *)key;
- (void)setRelationConditionWithObject:(id)object forKey:(NSString *)key;

- (void)removeAllConditions;

///--------------------------------------
#pragma mark - Sort
///--------------------------------------

- (void)sortByKey:(NSString *)key ascending:(BOOL)ascending;
- (void)addSortKey:(NSString *)key ascending:(BOOL)ascending;
- (void)addSortKeysFromSortDescriptors:(NSArray<NSSortDescriptor *> *)sortDescriptors;

///--------------------------------------
#pragma mark - Includes
///--------------------------------------

- (void)includeKey:(NSString *)key;
- (void)includeKeys:(NSArray<NSString *> *)keys;

///--------------------------------------
#pragma mark - Selected Keys
///--------------------------------------

- (void)selectKeys:(NSArray<NSString *> *)keys;

///--------------------------------------
#pragma mark - Redirect
///--------------------------------------

- (void)redirectClassNameForKey:(NSString *)key;

@end
