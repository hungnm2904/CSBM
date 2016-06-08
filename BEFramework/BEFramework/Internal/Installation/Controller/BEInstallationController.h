//
//  BEInstallationController.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BECoreDataProvider.h"
#import "BEObjectControlling.h"

@interface BEInstallationController : NSObject <BEObjectControlling>

@property (nonatomic, weak, readonly) id<BEObjectControllerProvider, BECurrentInstallationControllerProvider> dataSource;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init NS_UNAVAILABLE;
- (instancetype)initWithDataSource:(id<BEObjectControllerProvider, BECurrentInstallationControllerProvider>)dataSource;
+ (instancetype)controllerWithDataSource:(id<BEObjectControllerProvider, BECurrentInstallationControllerProvider>)dataSource;

@end