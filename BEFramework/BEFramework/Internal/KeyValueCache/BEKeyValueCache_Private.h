//
//  BEKeyValueCache_Private.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEKeyValueCache.h"

@interface BEKeyValueCache ()

///--------------------------------------
#pragma mark - Properties
///--------------------------------------

@property (nullable, nonatomic, strong, readwrite) NSFileManager *fileManager;
@property (nullable, nonatomic, strong, readwrite) NSCache *memoryCache;

@property (nonatomic, assign) NSUInteger maxDiskCacheBytes;
@property (nonatomic, assign) NSUInteger maxDiskCacheRecords;
@property (nonatomic, assign) NSUInteger maxMemoryCacheBytesPerRecord;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithCacheDirectoryURL:(nullable NSURL *)url
                              fileManager:(nullable NSFileManager *)fileManager
                              memoryCache:(nullable NSCache *)cache NS_DESIGNATED_INITIALIZER;

///--------------------------------------
#pragma mark - Waiting
///--------------------------------------

- (void)waitForOutstandingOperations;

@end

@interface BEKeyValueCacheEntry : NSObject

///--------------------------------------
#pragma mark - Properties
///--------------------------------------

@property (atomic, copy, readonly) NSString *value;
@property (atomic, strong, readonly) NSDate *creationTime;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

+ (instancetype)cacheEntryWithValue:(NSString *)value;
+ (instancetype)cacheEntryWithValue:(NSString *)value creationTime:(NSDate *)creationTime;

- (instancetype)init NS_UNAVAILABLE;
- (instancetype)initWithValue:(NSString *)value;
- (instancetype)initWithValue:(NSString *)value
                 creationTime:(NSDate *)creationTime NS_DESIGNATED_INITIALIZER;

@end