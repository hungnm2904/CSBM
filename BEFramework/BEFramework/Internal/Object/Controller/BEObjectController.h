//
//  BEObjectController.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEDataProvider.h"
#import "BEObjectControlling.h"

@class BFTask<__covariant BFGenericType>;
@class BEObject;


@interface BEObjectController : NSObject <BEObjectControlling>

@property (nonatomic, weak, readonly) id<BECommandRunnerProvider> dataSource;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

- (instancetype)initWithDataSource:(id<BECommandRunnerProvider>)dataSource NS_DESIGNATED_INITIALIZER;
+ (instancetype)controllerWithDataSource:(id<BECommandRunnerProvider>)dataSource;

@end
