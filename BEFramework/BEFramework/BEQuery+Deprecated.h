//
//  BEQuery+Deprecated.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEQuery.h"

@interface BEQuery (Deprecated)

///--------------------------------------
#pragma mark - Getting Objects by ID
///--------------------------------------

/**
 Gets a `BEObject` asynchronously.
 
 This mutates the BEQuery. It will reset limit to `1`, skip to `0` and remove all conditions, leaving only `objectId`.
 
 @param objectId The id of the object being requested.
 @param target The target for the callback selector.
 @param selector The selector for the callback.
 It should have the following signature: `(void)callbackWithResult:(id)result error:(NSError *)error`.
 Result will be `nil` if error is set and vice versa.
 
 @deprecated Please use `BEQuery.-getObjectInBackgroundWithId:block:` instead.
 */
- (void)getObjectInBackgroundWithId:(NSString *)objectId
                             target:(nullable id)target
                           selector:(nullable SEL)selector PARSE_DEPRECATED("Please use `BEQuery.-getObjectInBackgroundWithId:block:` instead.");

///--------------------------------------
#pragma mark - Getting all Matches for a Query
///--------------------------------------

/**
 Finds objects *asynchronously* and calls the given callback with the results.
 
 @param target The object to call the selector on.
 @param selector The selector to call.
 It should have the following signature: `(void)callbackWithResult:(id)result error:(NSError *)error`.
 Result will be `nil` if error is set and vice versa.
 
 @deprecated Please use `BEQuery.-findObjectsInBackgroundWithBlock:` instead.
 */
- (void)findObjectsInBackgroundWithTarget:(nullable id)target
                                 selector:(nullable SEL)selector PARSE_DEPRECATED("Please use `BEQuery.-findObjectsInBackgroundWithBlock:` instead.");

///--------------------------------------
#pragma mark - Getting the First Match in a Query
///--------------------------------------

/**
 Gets an object *asynchronously* and calls the given callback with the results.
 
 @warning This method mutates the query. It will reset the limit to `1`.
 
 @param target The object to call the selector on.
 @param selector The selector to call.
 It should have the following signature: `(void)callbackWithResult:(BEObject *)result error:(NSError *)error`.
 `result` will be `nil` if `error` is set OR no object was found matching the query.
 `error` will be `nil` if `result` is set OR if the query succeeded, but found no results.
 
 @deprecated Please use `BEQuery.-getFirstObjectInBackgroundWithBlock:` instead.
 */
- (void)getFirstObjectInBackgroundWithTarget:(nullable id)target
                                    selector:(nullable SEL)selector PARSE_DEPRECATED("Please use `BEQuery.-getFirstObjectInBackgroundWithBlock:` instead.");

///--------------------------------------
#pragma mark - Counting the Matches in a Query
///--------------------------------------

/**
 Counts objects *asynchronously* and calls the given callback with the count.
 
 @param target The object to call the selector on.
 @param selector The selector to call.
 It should have the following signature: `(void)callbackWithResult:(NSNumber *)result error:(NSError *)error`.
 
 @deprecated Please use `BEQuery.-countObjectsInBackgroundWithBlock:` instead.
 */
- (void)countObjectsInBackgroundWithTarget:(nullable id)target
                                  selector:(nullable SEL)selector PARSE_DEPRECATED("Please use `BEQuery.-countObjectsInBackgroundWithBlock:` instead.");

@end
