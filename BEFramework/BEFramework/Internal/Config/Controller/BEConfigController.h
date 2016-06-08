//
//  BEConfigController.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEConstants.h"
#import "BEDataProvider.h"

@class BFTask<__covariant BFGenericType>;
@class BEConfig;
@class BECurrentConfigController;

@interface BEConfigController : NSObject

@property (nonatomic, weak, readonly) id<BEPersistenceControllerProvider, BECommandRunnerProvider> dataSource;

@property (nonatomic, strong, readonly) BECurrentConfigController *currentConfigController;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

- (instancetype)initWithDataSource:(id<BEPersistenceControllerProvider, BECommandRunnerProvider>)dataSource NS_DESIGNATED_INITIALIZER;

///--------------------------------------
#pragma mark - Fetch
///--------------------------------------

/**
 Fetches current config from network async.
 
 @param sessionToken Current user session token.
 
 @return `BFTask` with result set to `BEConfig`.
 */
- (BFTask *)fetchConfigAsyncWithSessionToken:(NSString *)sessionToken;

@end
