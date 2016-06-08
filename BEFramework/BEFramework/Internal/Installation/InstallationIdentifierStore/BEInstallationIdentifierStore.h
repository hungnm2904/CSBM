//
//  BEInstallatioinIdentifierStore.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "BEDataProvider.h"
#import "BEConstants.h"

@interface BEInstallationIdentifierStore : NSObject

@property (nonatomic, weak, readonly) id<BEPersistenceControllerProvider> dataSource;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

- (instancetype)initWithDataSource:(id<BEPersistenceControllerProvider>)dataSource NS_DESIGNATED_INITIALIZER;


@end
