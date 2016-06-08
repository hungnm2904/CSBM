//
//  BECurrentObjectControlling.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEConstants.h"

@class BFTask<__covariant BFGenericType>;
@class BEObject;

typedef NS_ENUM(NSUInteger, BECurrentObjectStorageType) {
  BECurrentObjectStorageTypeFile = 1,
  BECurrentObjectStorageTypeOfflineStore,
};

@protocol BECurrentObjectControlling <NSObject>

@property (nonatomic, assign, readonly) BECurrentObjectStorageType storageType;

///--------------------------------------
#pragma mark - Current
///--------------------------------------

- (BFTask *)getCurrentObjectAsync;
- (BFTask *)saveCurrentObjectAsync:(BEObject *)object;

@end

