//
//  BECurrentConfigController.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BECurrentConfigController.h"
#import "BFTask+Private.h"
#import "BEAssert.h"
#import "BEConfig_Private.h"
#import "BEDecoder.h"
#import "BEJSONSerialization.h"
#import "BEAsyncTaskQueue.h"
#import "BEEncoder.h"

static NSString *const BEConfigCurrentConfigFileName_ = @"config";

@interface BECurrentConfigController () {
  BEAsyncTaskQueue *_dataTaskQueue;
  BEConfig *_currentConfig;
}

@property (nonatomic, copy, readonly) NSString *configFilePath;

@end

@implementation BECurrentConfigController

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithDataSource:(id<BEPersistenceControllerProvider>)dataSource {
  self = [super init];
  if (!self) return nil;
  
  _dataTaskQueue = [[BEAsyncTaskQueue alloc] init];
  
  _dataSource = dataSource;
  
  return self;
}

+ (instancetype)controllerWithDataSource:(id<BEPersistenceControllerProvider>)dataSource {
  return [[self alloc] initWithDataSource:dataSource];
}

///--------------------------------------
#pragma mark - Accessors
///--------------------------------------

//- (BFTask *)getCurrentConfigAsync {
//  return [_dataTaskQueue enqueue:^id(BFTask *_) {
//    if (!_currentConfig) {
//      return [[self _loadConfigAsync] continueWithSuccessBlock:^id(BFTask<BEConfig *> *task) {
//        _currentConfig = task.result;
//        return _currentConfig;
//      }];
//    }
//    return _currentConfig;
//  }];
//}

//- (BFTask *)setCurrentConfigAsync:(BEConfig *)config {
//  @weakify(self);
//  return [_dataTaskQueue enqueue:^id(BFTask *_) {
//    @strongify(self);
//    _currentConfig = config;
//    
//    NSDictionary *configParameters = @{ BEConfigParametersRESTKey : (config.parametersDictionary ?: @{}) };
//    id encodedObject = [[BEPointerObjectEncoder objectEncoder] encodeObject:configParameters];
//    NSData *jsonData = [BEJSONSerialization dataFromJSONObject:encodedObject];
//    return [[self _getPersistenceGroupAsync] continueWithSuccessBlock:^id(BFTask<id<BEPersistenceGroup>> *task) {
//      return [task.result setDataAsync:jsonData forKey:BEConfigCurrentConfigFileName_];
//    }];
//  }];
//}
//
//- (BFTask *)clearCurrentConfigAsync {
//  @weakify(self);
//  return [_dataTaskQueue enqueue:^id(BFTask *_) {
//    @strongify(self);
//    _currentConfig = nil;
//    return [[self.dataSource.persistenceController getPersistenceGroupAsync] continueWithSuccessBlock:^id(BFTask<id<BEPersistenceGroup>> *task) {
//      return [task.result removeDataAsyncForKey:BEConfigCurrentConfigFileName_];
//    }];
//  }];
//}

- (BFTask *)clearMemoryCachedCurrentConfigAsync {
  return [_dataTaskQueue enqueue:^id(BFTask *_) {
    _currentConfig = nil;
    return nil;
  }];
}

/////--------------------------------------
//#pragma mark - Data
/////--------------------------------------
//
//- (BFTask<BEConfig *> *)_loadConfigAsync {
//  return [[[self _getPersistenceGroupAsync] continueWithSuccessBlock:^id(BFTask<id<BEPersistenceGroup>> *task) {
//    return [task.result getDataAsyncForKey:BEConfigCurrentConfigFileName_];
//  }] continueWithSuccessBlock:^id(BFTask *task) {
//    if (task.result) {
//      NSDictionary *dictionary = [BEJSONSerialization JSONObjectFromData:task.result];
//      if (dictionary) {
//        NSDictionary *decodedDictionary = [[BEDecoder objectDecoder] decodeObject:dictionary];
//        return [[BEConfig alloc] initWithFetchedConfig:decodedDictionary];
//      }
//    }
//    return [[BEConfig alloc] init];
//  }];
//}
//
/////--------------------------------------
//#pragma mark - Convenience
/////--------------------------------------
//
//- (BFTask<id<BEPersistenceGroup>> *)_getPersistenceGroupAsync {
//  return [self.dataSource.persistenceController getPersistenceGroupAsync];
//}

@end