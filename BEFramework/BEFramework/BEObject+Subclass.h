//
//  BEObject+Subclass.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEObject.h"

@class BEQuery<BEGenericObject: BEObject *>;

@interface BEObject (Subclass)

///--------------------------------------
#pragma mark - Methods for Subclasses
///--------------------------------------

/**
 Creates an instance of the registered subclass with this class's `PFSubclassing.+parseClassName`.
 
 This helps a subclass ensure that it can be subclassed itself.
 For example, `[PFUser object]` will return a `MyUser` object if `MyUser` is a registered subclass of `PFUser`.
 For this reason, `[MyClass object]` is preferred to `[[MyClass alloc] init]`.
 This method can only be called on subclasses which conform to `PFSubclassing`.
 */
+ (instancetype)object;

/**
 The following ignore statement is required, as otherwise, every time this is compiled - it produces an `swift_name` unused warning.
 This appears to be a clang itself or ClangImporter issue when imported into Swift.
 */

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wignored-attributes"

/**
 Creates a reference to an existing `PFObject` for use in creating associations between `PFObjects`.
 
 Calling `dataAvailable` on this object will return `NO` until `-fetchIfNeeded` or `-fetch` has been called.
 This method can only be called on subclasses which conform to `PFSubclassing`.
 A default implementation is provided by `PFObject` which should always be sufficient.
 No network request will be made.
 
 @param objectId The object id for the referenced object.
 
 @return An instance of `PFObject` without data.
 */
+ (instancetype)objectWithoutDataWithObjectId:(nullable NSString *)objectId NS_SWIFT_NAME(init(withoutDataWithObjectId:));

#pragma clang diagnostic pop

/**
 Registers an Objective-C class for Parse to use for representing a given Parse class.
 
 Once this is called on a `PFObject` subclass, any `PFObject` Parse creates with a class name
 that matches `[self parseClassName]` will be an instance of subclass.
 This method can only be called on subclasses which conform to `PFSubclassing`.
 A default implementation is provided by `PFObject` which should always be sufficient.
 */
+ (void)registerSubclass;

/**
 Returns a query for objects of type `PFSubclassing.+parseClassName`.
 
 This method can only be called on subclasses which conform to `PFSubclassing`.
 A default implementation is provided by `PFObject` which should always be sufficient.
 
 @see `PFQuery`
 */
+ (nullable BEQuery *)query;

/**
 Returns a query for objects of type `PFSubclassing.+parseClassName` with a given predicate.
 
 A default implementation is provided by `PFObject` which should always be sufficient.
 @warning This method can only be called on subclasses which conform to `PFSubclassing`.
 
 @param predicate The predicate to create conditions from.
 
 @return An instance of `PFQuery`.
 
 @see `PFQuery.+queryWithClassName:predicate:`
 */
+ (nullable BEQuery *)queryWithPredicate:(nullable NSPredicate *)predicate;

@end
