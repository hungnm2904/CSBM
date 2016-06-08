//
//  BEObjectControlling.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEConstants.h"
#import "BEMacros.h"

@class BFTask<__covariant BFGenericType>;
@class PFObject;



@protocol BEObjectControlling <NSObject>

///--------------------------------------
#pragma mark - Fetch
///--------------------------------------

/**
 Fetches an object asynchronously.
 
 @param object       Object to fetch.
 @param sessionToken Session token to use.
 
 @return `BFTask` with result set to `PFObject`.
 */
- (BFTask *)fetchObjectAsync:(BEObject *)object withSessionToken:(nullable NSString *)sessionToken;

- (BFTask *)processFetchResultAsync:(NSDictionary *)result forObject:(PFObject *)object;

///--------------------------------------
#pragma mark - Delete
///--------------------------------------

/**
 Deletes an object asynchronously.
 
 @param object       Object to fetch.
 @param sessionToken Session token to use.
 
 @return `BFTask` with result set to `nil`.
 */
- (BFTask *)deleteObjectAsync:(BEObject *)object withSessionToken:(nullable NSString *)sessionToken;

//TODO: (nlutsenko) This needs removal, figure out how to kill it.
- (BFTask *)processDeleteResultAsync:(nullable NSDictionary *)result forObject:(BEObject *)object;

@end
