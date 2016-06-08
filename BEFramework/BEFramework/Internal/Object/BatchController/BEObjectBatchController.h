//
//  BEObjectBatchController.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEConstants.h"
#import "BEDataProvider.h"
#import "BEMacros.h"

@class BFTask<__covariant BFGenericType>;
@class BEObject;


@interface BEObjectBatchController : NSObject

@property (nonatomic, weak, readonly) id<BECommandRunnerProvider> dataSource;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

- (instancetype)initWithDataSource:(id<BECommandRunnerProvider>)dataSource NS_DESIGNATED_INITIALIZER;
+ (instancetype)controllerWithDataSource:(id<BECommandRunnerProvider>)dataSource;

///--------------------------------------
#pragma mark - Fetch
///--------------------------------------

- (BFTask *)fetchObjectsAsync:(nullable NSArray *)objects withSessionToken:(nullable NSString *)sessionToken;

///--------------------------------------
#pragma mark - Delete
///--------------------------------------

- (BFTask *)deleteObjectsAsync:(nullable NSArray *)objects withSessionToken:(nullable NSString *)sessionToken;

///--------------------------------------
#pragma mark - Utilities
///--------------------------------------

+ (nullable NSArray *)uniqueObjectsArrayFromArray:(nullable NSArray *)objects omitObjectsWithData:(BOOL)omitFetched;
+ (NSArray *)uniqueObjectsArrayFromArray:(NSArray *)objects usingFilter:(BOOL (^)(BEObject *object))filter;

@end