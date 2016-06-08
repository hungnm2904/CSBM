//
//  BEManager.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEManager.h"
#import <Bolts/BFExecutor.h>
#import "BEClientConfiguration.h"
#import "BEConstants.h"
#import "BEDataProvider.h"
#import "BEMacros.h"

#import "BFTask+Private.h"
#import "BEAssert.h"
#import "BEConfig.h"
#import "BECoreManager.h"
#import "BELogging.h"
#import "BEUser.h"

static NSString *const _CSBMApplicationIdFileName = @"applicationId";

@interface BEManager () <BECoreManagerDataSource>
{
  
  dispatch_queue_t _coreManagerAccessQueue;
  dispatch_queue_t _controllerAccessQueue;
  
  dispatch_queue_t _preloadQueue;
}

@end

@implementation BEManager

@synthesize installationIdentifierStore = _installationIdentifierStore;
@synthesize commandRunner = _commandRunner;
@synthesize coreManager = _coreManager;

- (instancetype)initWithConfiguration:(BEClientConfiguration *)configuration {
  self = [super init];
  if (!self) return nil;
  
  _coreManagerAccessQueue = dispatch_queue_create("com.parse.coreManager.access", DISPATCH_QUEUE_SERIAL);
  _controllerAccessQueue = dispatch_queue_create("com.parse.controller.access", DISPATCH_QUEUE_SERIAL);
  _preloadQueue = dispatch_queue_create("com.parse.preload", DISPATCH_QUEUE_SERIAL);
  
  _configuration = [configuration copy];
  
  return self;
}

- (void)startManaging {
  // Migrate any data if it's required.
  //[self _migrateSandboxDataToApplicationGroupContainerIfNeeded];
  
  // TODO: (nlutsenko) Make it not terrible!
  //[[self.persistenceController getPersistenceGroupAsync] waitForResult:nil withMainThreadWarning:NO];
  
//  if (self.configuration.localDatastoreEnabled) {
//    BEOfflineStoreOptions options = (self.configuration.applicationGroupIdentifier ?
//                                     BEOfflineStoreOptionAlwaysFetchFromSQLite : 0);
//    [self loadOfflineStoreWithOptions:options];
//  }
}
- (BECoreManager *)coreManager {
  __block BECoreManager *manager = nil;
  dispatch_sync(_coreManagerAccessQueue, ^{
    if (!_coreManager) {
      _coreManager = [BECoreManager managerWithDataSource:self];
    }
    manager = _coreManager;
  });
  return manager;
}

- (void)unloadCoreManager {
  dispatch_sync(_coreManagerAccessQueue, ^{
    _coreManager = nil;
  });
}


@end
