//
//  BEKeyValueCache.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface BEKeyValueCache : NSObject
@property (nonatomic, copy, readonly) NSString *cacheDirectoryPath;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

- (instancetype)initWithCacheDirectoryPath:(NSString *)path;

///--------------------------------------
#pragma mark - Setting
///--------------------------------------

- (void)setObject:(NSString *)object forKey:(NSString *)key;
- (void)setObject:(NSString *)object forKeyedSubscript:(NSString *)key;

///--------------------------------------
#pragma mark - Getting
///--------------------------------------

- (NSString *)objectForKey:(NSString *)key maxAge:(NSTimeInterval)age;

///--------------------------------------
#pragma mark - Removing
///--------------------------------------

- (void)removeObjectForKey:(NSString *)key;
- (void)removeAllObjects;
@end
