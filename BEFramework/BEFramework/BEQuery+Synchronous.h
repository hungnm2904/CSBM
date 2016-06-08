//
//  BEQuery+Synchronous.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEConstants.h"
#import "BEQuery.h"
#import "BEObject.h"

@interface BEQuery<BEGenericObject: BEObject *> (Synchronous)

///--------------------------------------
#pragma mark - Getting Objects by ID
///--------------------------------------

/**
 Returns a `BEObject` with a given class and id.
 
 @param objectClass The class name for the object that is being requested.
 @param objectId The id of the object that is being requested.
 
 @return The `BEObject` if found. Returns `nil` if the object isn't found, or if there was an error.
 */
+ (nullable BEGenericObject)getObjectOfClass:(NSString *)objectClass objectId:(NSString *)objectId BE_SWIFT_UNAVAILABLE;

/**
 Returns a `BEObject` with a given class and id and sets an error if necessary.
 
 @param objectClass The class name for the object that is being requested.
 @param objectId The id of the object that is being requested.
 @param error Pointer to an `NSError` that will be set if necessary.
 
 @return The `BEObject` if found. Returns `nil` if the object isn't found, or if there was an `error`.
 */
+ (nullable BEGenericObject)getObjectOfClass:(NSString *)objectClass objectId:(NSString *)objectId error:(NSError **)error;

/**
 Returns a `BEObject` with the given id.
 
 @warning This method mutates the query.
 It will reset limit to `1`, skip to `0` and remove all conditions, leaving only `objectId`.
 
 @param objectId The id of the object that is being requested.
 
 @return The `BEObject` if found. Returns nil if the object isn't found, or if there was an error.
 */
- (nullable BEGenericObject)getObjectWithId:(NSString *)objectId BE_SWIFT_UNAVAILABLE;

/**
 Returns a `BEObject` with the given id and sets an error if necessary.
 
 @warning This method mutates the query.
 It will reset limit to `1`, skip to `0` and remove all conditions, leaving only `objectId`.
 
 @param objectId The id of the object that is being requested.
 @param error Pointer to an `NSError` that will be set if necessary.
 
 @return The `BEObject` if found. Returns nil if the object isn't found, or if there was an error.
 */
- (nullable BEGenericObject)getObjectWithId:(NSString *)objectId error:(NSError **)error;

///--------------------------------------
#pragma mark - Getting User Objects
///--------------------------------------

/**
 Returns a `BEUser` with a given id.
 
 @param objectId The id of the object that is being requested.
 
 @return The BEUser if found. Returns nil if the object isn't found, or if there was an error.
 */
+ (nullable BEUser *)getUserObjectWithId:(NSString *)objectId BE_SWIFT_UNAVAILABLE;

/**
 Returns a BEUser with a given class and id and sets an error if necessary.
 @param objectId The id of the object that is being requested.
 @param error Pointer to an NSError that will be set if necessary.
 @result The BEUser if found. Returns nil if the object isn't found, or if there was an error.
 */
+ (nullable BEUser *)getUserObjectWithId:(NSString *)objectId error:(NSError **)error;

///--------------------------------------
#pragma mark - Getting all Matches for a Query
///--------------------------------------

/**
 Finds objects *synchronously* based on the constructed query.
 
 @return Returns an array of `BEObject` objects that were found.
 */
- (nullable NSArray<BEGenericObject> *)findObjects BE_SWIFT_UNAVAILABLE;

/**
 Finds objects *synchronously* based on the constructed query and sets an error if there was one.
 
 @param error Pointer to an `NSError` that will be set if necessary.
 
 @return Returns an array of `BEObject` objects that were found.
 */
- (nullable NSArray<BEGenericObject> *)findObjects:(NSError **)error;

///--------------------------------------
#pragma mark - Getting the First Match in a Query
///--------------------------------------

/**
 Gets an object *synchronously* based on the constructed query.
 
 @warning This method mutates the query. It will reset the limit to `1`.
 
 @return Returns a `BEObject`, or `nil` if none was found.
 */
- (nullable BEGenericObject)getFirstObject BE_SWIFT_UNAVAILABLE;

/**
 Gets an object *synchronously* based on the constructed query and sets an error if any occurred.
 
 @warning This method mutates the query. It will reset the limit to `1`.
 
 @param error Pointer to an `NSError` that will be set if necessary.
 
 @return Returns a `BEObject`, or `nil` if none was found.
 */
- (nullable BEGenericObject)getFirstObject:(NSError **)error;

///--------------------------------------
#pragma mark - Counting the Matches in a Query
///--------------------------------------

/**
 Counts objects *synchronously* based on the constructed query.
 
 @return Returns the number of `BEObject` objects that match the query, or `-1` if there is an error.
 */
- (NSInteger)countObjects BE_SWIFT_UNAVAILABLE;

/**
 Counts objects *synchronously* based on the constructed query and sets an error if there was one.
 
 @param error Pointer to an `NSError` that will be set if necessary.
 
 @return Returns the number of `BEObject` objects that match the query, or `-1` if there is an error.
 */
- (NSInteger)countObjects:(NSError **)error;

@end
