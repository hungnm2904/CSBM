//
//  BESubclassing.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

@class BEQuery<BEGenericObject : BEObject *>;

@protocol BESubclassing <NSObject>

@required

/**
 The name of the class as seen in the REST API.
 */
+ (NSString *)csbmClassName;

@optional

/**
 Constructs an object of the most specific class known to implement `+parseClassName`.
 
 This method takes care to help `BEObject` subclasses be subclassed themselves.
 For example, `BEUser.+object` returns a `BEUser` by default but will return an
 object of a registered subclass instead if one is known.
 A default implementation is provided by `BEObject` which should always be sufficient.
 
 @return Returns the object that is instantiated.
 */
+ (instancetype)object;

/**
 Creates a reference to an existing BEObject for use in creating associations between BEObjects.
 
 Calling `BEObject.dataAvailable` on this object will return `NO`
 until `BEObject.-fetchIfNeeded` has been called. No network request will be made.
 A default implementation is provided by `BEObject` which should always be sufficient.
 
 @param objectId The object id for the referenced object.
 
 @return A new `BEObject` without data.
 */
+ (instancetype)objectWithoutDataWithObjectId:(nullable NSString *)objectId BE_SWIFT_UNAVAILABLE;

/**
 Create a query which returns objects of this type.
 
 A default implementation is provided by `BEObject` which should always be sufficient.
 */
+ (nullable BEQuery *)query;

/**
 Returns a query for objects of this type with a given predicate.
 
 A default implementation is provided by `BEObject` which should always be sufficient.
 
 @param predicate The predicate to create conditions from.
 
 @return An instance of `BEQuery`.
 
 @see [BEQuery queryWithClassName:predicate:]
 */
+ (nullable BEQuery *)queryWithPredicate:(nullable NSPredicate *)predicate;

/**
 Lets Parse know this class should be used to instantiate all objects with class type `parseClassName`.
 
 @warning This method must be called before `Parse.+setApplicationId:clientKey:`.
 */
+ (void)registerSubclass;

@end