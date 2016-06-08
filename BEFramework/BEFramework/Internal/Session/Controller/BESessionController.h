//
//  BESessionController.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEConstants.h"
#import "BEDataProvider.h"

@class BFTask<__covariant BFGenericType>;
@class BESession;

NS_ASSUME_NONNULL_BEGIN

@interface BESessionController : NSObject

@property (nonatomic, weak, readonly) id<BECommandRunnerProvider> dataSource;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithDataSource:(id<BECommandRunnerProvider>)dataSource;
+ (instancetype)controllerWithDataSource:(id<BECommandRunnerProvider>)dataSource;

///--------------------------------------
#pragma mark - Current Session
///--------------------------------------

- (BFTask *)getCurrentSessionAsyncWithSessionToken:(nullable NSString *)sessionToken;

@end

NS_ASSUME_NONNULL_END
