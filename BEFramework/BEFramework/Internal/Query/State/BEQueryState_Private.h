//
//  BEQueryState_Private.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEMacros.h"
#import "BEQueryState.h"

#define BEQueryStatePropertyName(NAME) @keypath(BEQueryState, NAME)

@interface BEQueryState () {
@protected
  NSString *_parseClassName;
  
  NSDictionary<NSString *, id> *_conditions;
  
  NSArray<NSString *> *_sortKeys;
  
  NSSet<NSString *> *_includedKeys;
  NSSet<NSString *> *_selectedKeys;
  NSDictionary<NSString *, NSString *> *_extraOptions;
  
  NSInteger _limit;
  NSInteger _skip;
  
  BECachePolicy _cachePolicy;
  NSTimeInterval _maxCacheAge;
  
  BOOL _trace;
  
  BOOL _shouldIgnoreACLs;
  BOOL _shouldIncludeDeletingEventually;
  BOOL _queriesLocalDatastore;
  NSString *_localDatastorePinName;
}

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

@end
