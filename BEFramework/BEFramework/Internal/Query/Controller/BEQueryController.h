//
//  BEQueryController.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "BEConstants.h"
#import "BEDataProvider.h"

@class BFCancellationToken;

@class BFTask<__covariant BFGenericType>;
@class BEQueryState;
@class BERESTCommand;
@class BECommandResult;
@class BEUser;



@interface BEQueryController : NSObject

@property (nonatomic, weak, readonly) id<BECommandRunnerProvider> commonDataSource;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

- (instancetype)initWithCommonDataSource:(id<BECommandRunnerProvider>)dataSource NS_DESIGNATED_INITIALIZER;

+ (instancetype)controllerWithCommonDataSource:(id<BECommandRunnerProvider>)dataSource;

///--------------------------------------
#pragma mark - Find
///--------------------------------------

/**
 Finds objects from network or LDS for any given query state.
 Supports cancellation and ACLed changes for a specific user.
 
 @param queryState        Query state to use.
 @param cancellationToken Cancellation token or `nil`.
 @param user              `user` to use for ACLs or `nil`.
 
 @return Task that resolves to `NSArray` of `BEObject`s.
 */
- (BFTask *)findObjectsAsyncForQueryState:(BEQueryState *)queryState
                    withCancellationToken:(nullable BFCancellationToken *)cancellationToken
                                     user:(nullable BEUser *)user; // TODO: (nlutsenko) Pass `BEUserState` instead of user.

///--------------------------------------
#pragma mark - Count
///--------------------------------------

/**
 Counts objects from network or LDS for any given query state.
 Supports cancellation and ACLed changes for a specific user.
 
 @param queryState        Query state to use.
 @param cancellationToken Cancellation token or `nil`.
 @param user              `user` to use for ACLs or `nil`.
 
 @return Task that resolves to `NSNumber` with a count of results.
 */
- (BFTask *)countObjectsAsyncForQueryState:(BEQueryState *)queryState
                     withCancellationToken:(nullable BFCancellationToken *)cancellationToken
                                      user:(nullable BEUser *)user; // TODO: (nlutsenko) Pass `BEUserState` instead of user.

///--------------------------------------
#pragma mark - Caching
///--------------------------------------

- (NSString *)cacheKeyForQueryState:(BEQueryState *)queryState sessionToken:(nullable NSString *)sessionToken;
- (BOOL)hasCachedResultForQueryState:(BEQueryState *)queryState sessionToken:(nullable NSString *)sessionToken;

- (void)clearCachedResultForQueryState:(BEQueryState *)queryState sessionToken:(nullable NSString *)sessionToken;
- (void)clearAllCachedResults;

@end

@protocol BEQueryControllerSubclass <NSObject>

/**
 Implementation should run a command on a network runner.
 
 @param command           Command to run.
 @param cancellationToken Cancellation token.
 @param queryState        Query state to run command for.
 
 @return `BFTask` instance with result of `BECommandResult`.
 */
- (BFTask *)runNetworkCommandAsync:(BERESTCommand *)command
             withCancellationToken:(nullable BFCancellationToken *)cancellationToken
                     forQueryState:(BEQueryState *)queryState;

@end
