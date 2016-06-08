//
//  BECommandCache.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "BEConstants.h"
#import "BEEventuallyQueue.h"

@class BECommandCacheTestHelper;
@class BEObject;
@protocol BEObjectLocalIdStoreProvider;

/**
 ParseCommandCache manages an on-disk cache of commands to be executed, and a thread with a standard run loop
 that executes the commands.  There should only ever be one instance of this class, because multiple instances
 would be running separate threads trying to read and execute the same commands.
 */
@interface BECommandCache : BEEventuallyQueue

@property (nonatomic, weak, readonly) id<BEObjectLocalIdStoreProvider> coreDataSource;

@property (nonatomic, copy, readonly) NSString *diskCachePath;
@property (nonatomic, assign, readonly) unsigned long long diskCacheSize;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

/**
 Creates the command cache object for all ParseObjects with default configuration.
 This command cache is used to locally store save commands created by the [BEObject saveEventually].
 When a BECommandCache is instantiated, it will begin running its run loop,
 which will start by processing any commands already stored in the on-disk queue.
 */
+ (instancetype)newDefaultCommandCacheWithCommonDataSource:(id<BECommandRunnerProvider>)dataSource
                                            coreDataSource:(id<BEObjectLocalIdStoreProvider>)coreDataSource
                                           cacheFolderPath:(NSString *)cacheFolderPath;

- (instancetype)initWithDataSource:(id<BECommandRunnerProvider>)dataSource
                  maxAttemptsCount:(NSUInteger)attemptsCount
                     retryInterval:(NSTimeInterval)retryInterval NS_UNAVAILABLE;

- (instancetype)initWithDataSource:(id<BECommandRunnerProvider>)dataSource
                    coreDataSource:(id<BEObjectLocalIdStoreProvider>)coreDataSource
                  maxAttemptsCount:(NSUInteger)attemptsCount
                     retryInterval:(NSTimeInterval)retryInterval
                     diskCachePath:(NSString *)diskCachePath
                     diskCacheSize:(unsigned long long)diskCacheSize NS_DESIGNATED_INITIALIZER;

@end
