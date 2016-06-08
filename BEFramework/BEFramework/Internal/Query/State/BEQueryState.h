//
//  BEQueryState.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEConstants.h"
#import "BEBaseState.h"

@interface BEQueryState : BEBaseState <BEBaseStateSubclass, NSCopying, NSMutableCopying>

@property (nonatomic, copy, readonly) NSString *parseClassName;

@property (nonatomic, copy, readonly) NSDictionary<NSString *, id> *conditions;

@property (nonatomic, copy, readonly) NSArray<NSString *> *sortKeys;
@property (nonatomic, copy, readonly) NSString *sortOrderString;

@property (nonatomic, copy, readonly) NSSet<NSString *> *includedKeys;
@property (nonatomic, copy, readonly) NSSet<NSString *> *selectedKeys;
@property (nonatomic, copy, readonly) NSDictionary<NSString *, NSString *> *extraOptions;

@property (nonatomic, assign, readonly) NSInteger limit;
@property (nonatomic, assign, readonly) NSInteger skip;

///--------------------------------------
#pragma mark - Remote + Caching Options
///--------------------------------------

@property (nonatomic, assign, readonly) BECachePolicy cachePolicy;
@property (nonatomic, assign, readonly) NSTimeInterval maxCacheAge;

@property (nonatomic, assign, readonly) BOOL trace;

///--------------------------------------
#pragma mark - Local Datastore Options
///--------------------------------------

/**
 If ignoreACLs is enabled, we don't check ACLs when querying from LDS. We also don't grab
 `BEUser currentUser` since it's unnecessary when ignoring ACLs.
 */
@property (nonatomic, assign, readonly) BOOL shouldIgnoreACLs;
/**
 This is currently unused, but is here to allow future querying across objects that are in the
 process of being deleted eventually.
 */
@property (nonatomic, assign, readonly) BOOL shouldIncludeDeletingEventually;
@property (nonatomic, assign, readonly) BOOL queriesLocalDatastore;
@property (nonatomic, copy, readonly) NSString *localDatastorePinName;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithState:(BEQueryState *)state;
+ (instancetype)stateWithState:(BEQueryState *)state;

@end
