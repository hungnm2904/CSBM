//
//  BECurrentConfigController.h
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

@interface BECurrentConfigController : NSObject

@property (nonatomic, weak, readonly) id<BEPersistenceControllerProvider> dataSource;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

- (instancetype)initWithDataSource:(id<BEPersistenceControllerProvider>)dataSource NS_DESIGNATED_INITIALIZER;

+ (instancetype)controllerWithDataSource:(id<BEPersistenceControllerProvider>)dataSource;

///--------------------------------------
#pragma mark - Accessors
///--------------------------------------

- (BFTask *)getCurrentConfigAsync;
- (BFTask *)setCurrentConfigAsync:(BEConfig *)config;

- (BFTask *)clearCurrentConfigAsync;
- (BFTask *)clearMemoryCachedCurrentConfigAsync;

@end

