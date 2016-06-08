//
//  BECacheQueryController.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEQueryController.h"
#import "BEDataProvider.h"

@interface BECachedQueryController : BEQueryController <BEQueryControllerSubclass>

@property (nonatomic, weak, readonly) id<BECommandRunnerProvider, BEKeyValueCacheProvider> commonDataSource;

- (instancetype)initWithCommonDataSource:(id<BECommandRunnerProvider, BEKeyValueCacheProvider>)dataSource;
+ (instancetype)controllerWithCommonDataSource:(id<BECommandRunnerProvider, BEKeyValueCacheProvider>)dataSource;

@end

