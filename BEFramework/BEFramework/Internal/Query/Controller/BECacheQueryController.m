//
//  BECacheQueryController.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BECacheQueryController.h"

#import <Bolts/BFTask.h>
#import "BEAssert.h"
#import "BECommandResult.h"
#import "BECommandRunning.h"
#import "BEDecoder.h"
#import "BEErrorUtilities.h"
#import "BEJSONSerialization.h"
#import "BEMacros.h"
#import "BEQueryState.h"
#import "BERESTCommand.h"
#import "BERESTQueryCommand.h"
#import "BEUser.h"

#import "BEKeyValueCache.h"
@implementation BECachedQueryController

@dynamic commonDataSource;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithCommonDataSource:(id<BECommandRunnerProvider, BEKeyValueCacheProvider>)dataSource {
  return [super initWithCommonDataSource:dataSource];
}

+ (instancetype)controllerWithCommonDataSource:(id<BECommandRunnerProvider, BEKeyValueCacheProvider>)dataSource {
  return [super controllerWithCommonDataSource:dataSource];
}

///--------------------------------------
#pragma mark - BEQueryControllerSubclass
///--------------------------------------

- (BFTask *)runNetworkCommandAsync:(BERESTCommand *)command
             withCancellationToken:(BFCancellationToken *)cancellationToken
                     forQueryState:(BEQueryState *)queryState {
  if (cancellationToken.cancellationRequested) {
    return [BFTask cancelledTask];
  }
  
  switch (queryState.cachePolicy) {
      case kBECachePolicyIgnoreCache:
    {
      return [self _runNetworkCommandAsync:command
                     withCancellationToken:cancellationToken
                             forQueryState:queryState];
    }
      break;
      case kBECachePolicyNetworkOnly:
    {
      return [[self _runNetworkCommandAsync:command
                      withCancellationToken:cancellationToken
                              forQueryState:queryState] continueWithSuccessBlock:^id(BFTask *task) {
        return [self _saveCommandResultAsync:task.result forCommandCacheKey:command.cacheKey];
      } cancellationToken:cancellationToken];
    }
      break;
      case kBECachePolicyCacheOnly:
    {
      return [self _runNetworkCommandAsyncFromCache:command
                              withCancellationToken:cancellationToken
                                      forQueryState:queryState];
    }
      break;
      case kBECachePolicyNetworkElseCache: {
        // Don't retry for network-else-cache, because it just slows things down.
        BFTask *networkTask = [self _runNetworkCommandAsync:command
                                      withCancellationToken:cancellationToken
                                              forQueryState:queryState];
        @weakify(self);
        return [networkTask continueWithBlock:^id(BFTask *task) {
          @strongify(self);
          if (task.cancelled || task.exception) {
            return task;
          } else if (task.error) {
            return [self _runNetworkCommandAsyncFromCache:command
                                    withCancellationToken:cancellationToken
                                            forQueryState:queryState];
          }
          return [self _saveCommandResultAsync:task.result forCommandCacheKey:command.cacheKey];
        } cancellationToken:cancellationToken];
      }
      break;
      case kBECachePolicyCacheElseNetwork:
    {
      BFTask *cacheTask = [self _runNetworkCommandAsyncFromCache:command
                                           withCancellationToken:cancellationToken
                                                   forQueryState:queryState];
      @weakify(self);
      return [cacheTask continueWithBlock:^id(BFTask *task) {
        @strongify(self);
        if (task.error) {
          return [self _runNetworkCommandAsync:command
                         withCancellationToken:cancellationToken
                                 forQueryState:queryState];
        }
        return task;
      } cancellationToken:cancellationToken];
    }
      break;
      case kBECachePolicyCacheThenNetwork:
      BEConsistencyAssertionFailure(@"kBECachePolicyCacheThenNetwork is not implemented as a runner.");
      break;
    default:
      BEConsistencyAssertionFailure(@"Unrecognized cache policy: %d", queryState.cachePolicy);
      break;
  }
  return nil;
}

- (BFTask *)_runNetworkCommandAsync:(BERESTCommand *)command
              withCancellationToken:(BFCancellationToken *)cancellationToken
                      forQueryState:(BEQueryState *)queryState {
  BECommandRunningOptions options = 0;
  // We don't want retries on NetworkElseCache, but rather instantly back-off to cache.
  if (queryState.cachePolicy != kBECachePolicyNetworkElseCache) {
    options = BECommandRunningOptionsRetryIfFailed;
  }
  BFTask *networkTask = [self.commonDataSource.commandRunner runCommandAsync:command
                                                                 withOptions:options
                                                           cancellationToken:cancellationToken];
  return [networkTask continueWithSuccessBlock:^id(BFTask *task) {
    if (queryState.cachePolicy == kBECachePolicyNetworkOnly ||
        queryState.cachePolicy == kBECachePolicyNetworkElseCache ||
        queryState.cachePolicy == kBECachePolicyCacheElseNetwork) {
      return [self _saveCommandResultAsync:task.result forCommandCacheKey:command.cacheKey];
    }
    // Roll-forward the original result.
    return task;
  } cancellationToken:cancellationToken];
}

///--------------------------------------
#pragma mark - Cache
///--------------------------------------

- (NSString *)cacheKeyForQueryState:(BEQueryState *)queryState sessionToken:(NSString *)sessionToken {
  return [BERESTQueryCommand findCommandForQueryState:queryState withSessionToken:sessionToken].cacheKey;
}

- (BOOL)hasCachedResultForQueryState:(BEQueryState *)queryState sessionToken:(NSString *)sessionToken {
  // TODO: (nlutsenko) Once there is caching for `count`, the results for that command should also be checked.
  // TODO: (nlutsenko) We should cache this result.
  
  NSString *cacheKey = [self cacheKeyForQueryState:queryState sessionToken:sessionToken];
  return ([self.commonDataSource.keyValueCache objectForKey:cacheKey maxAge:queryState.maxCacheAge] != nil);
}

- (void)clearCachedResultForQueryState:(BEQueryState *)queryState sessionToken:(NSString *)sessionToken {
  // TODO: (nlutsenko) Once there is caching for `count`, the results for that command should also be cleared.
  NSString *cacheKey = [self cacheKeyForQueryState:queryState sessionToken:sessionToken];
  [self.commonDataSource.keyValueCache removeObjectForKey:cacheKey];
}

- (void)clearAllCachedResults {
  [self.commonDataSource.keyValueCache removeAllObjects];
}

- (BFTask *)_runNetworkCommandAsyncFromCache:(BERESTCommand *)command
                       withCancellationToken:(BFCancellationToken *)cancellationToken
                               forQueryState:(BEQueryState *)queryState {
  NSString *jsonString = [self.commonDataSource.keyValueCache objectForKey:command.cacheKey
                                                                    maxAge:queryState.maxCacheAge];
  if (!jsonString) {
    NSError *error = [BEErrorUtilities errorWithCode:kBEErrorCacheMiss
                                             message:@"Cache miss."
                                           shouldLog:NO];
    return [BFTask taskWithError:error];
  }
  
  NSDictionary *object = [BEJSONSerialization JSONObjectFromString:jsonString];
  if (!object) {
    NSError *error = [BEErrorUtilities errorWithCode:kBEErrorCacheMiss
                                             message:@"Cache contains corrupted JSON."];
    return [BFTask taskWithError:error];
  }
  
  NSDictionary *decodedObject = [[BEDecoder objectDecoder] decodeObject:object];
  
  BECommandResult *result = [BECommandResult commandResultWithResult:decodedObject
                                                        resultString:jsonString
                                                        httpResponse:nil];
  return [BFTask taskWithResult:result];
}

- (BFTask *)_saveCommandResultAsync:(BECommandResult *)result forCommandCacheKey:(NSString *)cacheKey {
  NSString *resultString = result.resultString;
  if (resultString) {
    self.commonDataSource.keyValueCache[cacheKey] = resultString;
  }
  // Roll-forward the original result.
  return [BFTask taskWithResult:result];
}

@end
