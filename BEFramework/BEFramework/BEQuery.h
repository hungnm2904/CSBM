//
//  BEQuery.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <Bolts/BFTask.h>
#import "BEConstants.h"
#import "BEObject.h"
#import "BEUser.h"

@interface BEQuery<BEGenericObject: BEObject *> : NSObject<NSCopying>

///--------------------------------------
#pragma mark - Blocks
///--------------------------------------

typedef void (^BEQueryArrayResultBlock)(NSArray<BEGenericObject> *_Nullable objects, NSError * _Nullable error);

#pragma mark - Creating a Query for a Class

/**
 *  Initializes the query with a class name.
 *
 *  @param className The class name.
 *
 *  @return object class name.
 */
- (instancetype)initWithClassName:(NSString *)className;


/**
 *  Returns a `BEQuery` for a given class.
 *
 *  @param className The class to query on
 *
 *  @return `BEQuery` object.
 */
+ (instancetype)queryWithClassName:(NSString *)className;

/**
 *  Creates a Query with the constraints given by predicate.
 Simple comparisons such as `=`, `!=`, `<`, `>`, `<=`, `>=`, and `BETWEEN`
 *
 *  @param className The class to query on.
 *  @param predicate The predicate to create conditions from.
 *
 *  @return query result.
 */
+ (instancetype)queryWithClassName:(NSString *)className predicate:(nullable NSPredicate *)predicate;

/**
 *  The class name to query for.
 */
@property (nonatomic, strong) NSString *csbmClassName;

///--------------------------------------
#pragma mark - Adding Basic Constraints
///--------------------------------------

/**
 Make the query include `PFObject`s that have a reference stored at the provided key.
 
 This has an effect similar to a join.  You can use dot notation to specify which fields in
 the included object are also fetch.
 
 @param key The key to load child `PFObject`s for.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)includeKey:(NSString *)key;

/**
 Make the query include `PFObject`s that have a reference stored at the provided keys.
 
 @param keys The keys to load child `PFObject`s for.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)includeKeys:(NSArray<NSString *> *)keys;

/**
 Make the query restrict the fields of the returned `PFObject`s to include only the provided keys.
 
 If this is called multiple times, then all of the keys specified in each of the calls will be included.
 
 @param keys The keys to include in the result.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)selectKeys:(NSArray<NSString *> *)keys;

/**
 Add a constraint that requires a particular key exists.
 
 @param key The key that should exist.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)whereKeyExists:(NSString *)key;

/**
 Add a constraint that requires a key not exist.
 
 @param key The key that should not exist.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)whereKeyDoesNotExist:(NSString *)key;

/**
 Add a constraint to the query that requires a particular key's object to be equal to the provided object.
 
 @param key The key to be constrained.
 @param object The object that must be equalled.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)whereKey:(NSString *)key equalTo:(id)object;

/**
 Add a constraint to the query that requires a particular key's object to be less than the provided object.
 
 @param key The key to be constrained.
 @param object The object that provides an upper bound.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)whereKey:(NSString *)key lessThan:(id)object;

/**
 Add a constraint to the query that requires a particular key's object
 to be less than or equal to the provided object.
 
 @param key The key to be constrained.
 @param object The object that must be equalled.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)whereKey:(NSString *)key lessThanOrEqualTo:(id)object;

/**
 Add a constraint to the query that requires a particular key's object
 to be greater than the provided object.
 
 @param key The key to be constrained.
 @param object The object that must be equalled.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)whereKey:(NSString *)key greaterThan:(id)object;

/**
 Add a constraint to the query that requires a particular key's
 object to be greater than or equal to the provided object.
 
 @param key The key to be constrained.
 @param object The object that must be equalled.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)whereKey:(NSString *)key greaterThanOrEqualTo:(id)object;

/**
 Add a constraint to the query that requires a particular key's object
 to be not equal to the provided object.
 
 @param key The key to be constrained.
 @param object The object that must not be equalled.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)whereKey:(NSString *)key notEqualTo:(id)object;

/**
 Add a constraint to the query that requires a particular key's object
 to be contained in the provided array.
 
 @param key The key to be constrained.
 @param array The possible values for the key's object.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)whereKey:(NSString *)key containedIn:(NSArray *)array;

/**
 Add a constraint to the query that requires a particular key's object
 not be contained in the provided array.
 
 @param key The key to be constrained.
 @param array The list of values the key's object should not be.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)whereKey:(NSString *)key notContainedIn:(NSArray *)array;

/**
 Add a constraint to the query that requires a particular key's array
 contains every element of the provided array.
 
 @param key The key to be constrained.
 @param array The array of values to search for.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)whereKey:(NSString *)key containsAllObjectsInArray:(NSArray *)array;

///--------------------------------------
#pragma mark - Adding String Constraints
///--------------------------------------

/**
 Add a regular expression constraint for finding string values that match the provided regular expression.
 
 @warning This may be slow for large datasets.
 
 @param key The key that the string to match is stored in.
 @param regex The regular expression pattern to match.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)whereKey:(NSString *)key matchesRegex:(NSString *)regex;

/**
 Add a regular expression constraint for finding string values that match the provided regular expression.
 
 @warning This may be slow for large datasets.
 
 @param key The key that the string to match is stored in.
 @param regex The regular expression pattern to match.
 @param modifiers Any of the following supported PCRE modifiers:
 - `i` - Case insensitive search
 - `m` - Search across multiple lines of input
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)whereKey:(NSString *)key
matchesRegex:(NSString *)regex
modifiers:(nullable NSString *)modifiers;

/**
 Add a constraint for finding string values that contain a provided substring.
 
 @warning This will be slow for large datasets.
 
 @param key The key that the string to match is stored in.
 @param substring The substring that the value must contain.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)whereKey:(NSString *)key containsString:(nullable NSString *)substring;

/**
 Add a constraint for finding string values that start with a provided prefix.
 
 This will use smart indexing, so it will be fast for large datasets.
 
 @param key The key that the string to match is stored in.
 @param prefix The substring that the value must start with.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)whereKey:(NSString *)key hasPrefix:(nullable NSString *)prefix;

/**
 Add a constraint for finding string values that end with a provided suffix.
 
 @warning This will be slow for large datasets.
 
 @param key The key that the string to match is stored in.
 @param suffix The substring that the value must end with.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)whereKey:(NSString *)key hasSuffix:(nullable NSString *)suffix;

///--------------------------------------
#pragma mark - Adding Subqueries
///--------------------------------------

/**
 Returns a `PFQuery` that is the `or` of the passed in queries.
 
 @param queries The list of queries to or together.
 
 @return An instance of `PFQuery` that is the `or` of the passed in queries.
 */
+ (instancetype)orQueryWithSubqueries:(NSArray<BEQuery *> *)queries;

/**
 Adds a constraint that requires that a key's value matches a value in another key
 in objects returned by a sub query.
 
 @param key The key that the value is stored.
 @param otherKey The key in objects in the returned by the sub query whose value should match.
 @param query The query to run.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)whereKey:(NSString *)key
matchesKey:(NSString *)otherKey
inQuery:(BEQuery *)query;

/**
 Adds a constraint that requires that a key's value `NOT` match a value in another key
 in objects returned by a sub query.
 
 @param key The key that the value is stored.
 @param otherKey The key in objects in the returned by the sub query whose value should match.
 @param query The query to run.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)whereKey:(NSString *)key
doesNotMatchKey:(NSString *)otherKey
inQuery:(BEQuery *)query;

/**
 Add a constraint that requires that a key's value matches a `PFQuery` constraint.
 
 @warning This only works where the key's values are `PFObject`s or arrays of `PFObject`s.
 
 @param key The key that the value is stored in
 @param query The query the value should match
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)whereKey:(NSString *)key matchesQuery:(BEQuery *)query;

/**
 Add a constraint that requires that a key's value to not match a `PFQuery` constraint.
 
 @warning This only works where the key's values are `PFObject`s or arrays of `PFObject`s.
 
 @param key The key that the value is stored in
 @param query The query the value should not match
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)whereKey:(NSString *)key doesNotMatchQuery:(BEQuery *)query;

///--------------------------------------
#pragma mark - Sorting
///--------------------------------------

/**
 Sort the results in *ascending* order with the given key.
 
 @param key The key to order by.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)orderByAscending:(NSString *)key;

/**
 Additionally sort in *ascending* order by the given key.
 
 The previous keys provided will precedence over this key.
 
 @param key The key to order by.
 */
- (instancetype)addAscendingOrder:(NSString *)key;

/**
 Sort the results in *descending* order with the given key.
 
 @param key The key to order by.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)orderByDescending:(NSString *)key;

/**
 Additionally sort in *descending* order by the given key.
 
 The previous keys provided will precedence over this key.
 
 @param key The key to order by.
 */
- (instancetype)addDescendingOrder:(NSString *)key;

/**
 Sort the results using a given sort descriptor.
 
 @warning If a `sortDescriptor` has custom `selector` or `comparator` - they aren't going to be used.
 
 @param sortDescriptor The `NSSortDescriptor` to use to sort the results of the query.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)orderBySortDescriptor:(NSSortDescriptor *)sortDescriptor;

/**
 Sort the results using a given array of sort descriptors.
 
 @warning If a `sortDescriptor` has custom `selector` or `comparator` - they aren't going to be used.
 
 @param sortDescriptors An array of `NSSortDescriptor` objects to use to sort the results of the query.
 
 @return The same instance of `PFQuery` as the receiver. This allows method chaining.
 */
- (instancetype)orderBySortDescriptors:(nullable NSArray<NSSortDescriptor *> *)sortDescriptors;

///--------------------------------------
#pragma mark - Getting Objects by ID
///--------------------------------------

/**
 Gets a `PFObject` asynchronously and calls the given block with the result.
 
 @warning This method mutates the query.
 It will reset limit to `1`, skip to `0` and remove all conditions, leaving only `objectId`.
 
 @param objectId The id of the object that is being requested.
 
 @return The task, that encapsulates the work being done.
 */
- (BFTask<BEGenericObject> *)getObjectInBackgroundWithId:(NSString *)objectId;

/**
 Gets a `PFObject` asynchronously and calls the given block with the result.
 
 @warning This method mutates the query.
 It will reset limit to `1`, skip to `0` and remove all conditions, leaving only `objectId`.
 
 @param objectId The id of the object that is being requested.
 @param block The block to execute.
 The block should have the following argument signature: `^(NSArray *object, NSError *error)`
 */
- (void)getObjectInBackgroundWithId:(NSString *)objectId
block:(nullable void (^)(BEGenericObject _Nullable object, NSError *_Nullable error))block;

///--------------------------------------
#pragma mark - Getting User Objects
///--------------------------------------

/**
 @deprecated Please use [PFUser query] instead.
 */
+ (instancetype)queryForUser PARSE_DEPRECATED("Use BEUser query] instead.");

///--------------------------------------
#pragma mark - Getting all Matches for a Query
///--------------------------------------

/**
 Finds objects *asynchronously* and sets the `NSArray` of `PFObject` objects as a result of the task.
 
 @return The task, that encapsulates the work being done.
 */
- (BFTask<NSArray<BEGenericObject> *> *)findObjectsInBackground;

/**
 Finds objects *asynchronously* and calls the given block with the results.
 
 @param block The block to execute.
 It should have the following argument signature: `^(NSArray *objects, NSError *error)`
 */
- (void)findObjectsInBackgroundWithBlock:(nullable BEQueryArrayResultBlock)block;

///--------------------------------------
#pragma mark - Getting the First Match in a Query
///--------------------------------------

/**
 Gets an object *asynchronously* and sets it as a result of the task.
 
 @warning This method mutates the query. It will reset the limit to `1`.
 
 @return The task, that encapsulates the work being done.
 */
- (BFTask<BEGenericObject> *)getFirstObjectInBackground;

/**
 Gets an object *asynchronously* and calls the given block with the result.
 
 @warning This method mutates the query. It will reset the limit to `1`.
 
 @param block The block to execute.
 It should have the following argument signature: `^(PFObject *object, NSError *error)`.
 `result` will be `nil` if `error` is set OR no object was found matching the query.
 `error` will be `nil` if `result` is set OR if the query succeeded, but found no results.
 */
- (void)getFirstObjectInBackgroundWithBlock:(nullable void (^)(BEGenericObject _Nullable object, NSError *_Nullable error))block;

///--------------------------------------
#pragma mark - Counting the Matches in a Query
///--------------------------------------

/**
 Counts objects *asynchronously* and sets `NSNumber` with count as a result of the task.
 
 @return The task, that encapsulates the work being done.
 */
- (BFTask<NSNumber *> *)countObjectsInBackground;

/**
 Counts objects *asynchronously* and calls the given block with the counts.
 
 @param block The block to execute.
 It should have the following argument signature: `^(int count, NSError *error)`
 */
- (void)countObjectsInBackgroundWithBlock:(nullable BEIntegerResultBlock)block;

///--------------------------------------
#pragma mark - Cancelling a Query
///--------------------------------------

/**
 Cancels the current network request (if any). Ensures that callbacks won't be called.
 */
- (void)cancel;

///--------------------------------------
#pragma mark - Paginating Results
///--------------------------------------

/**
 A limit on the number of objects to return. The default limit is `100`, with a
 maximum of 1000 results being returned at a time.
 
 @warning If you are calling `findObjects` with `limit = 1`, you may find it easier to use `getFirst` instead.
 */
@property (nonatomic, assign) NSInteger limit;

/**
 The number of objects to skip before returning any.
 */
@property (nonatomic, assign) NSInteger skip;

///--------------------------------------
#pragma mark - Controlling Caching Behavior
///--------------------------------------

/**
 The cache policy to use for requests.
 
 Not allowed when Pinning is enabled.
 
 @see fromLocalDatastore
 @see fromPin
 @see fromPinWithName:
 */
@property (nonatomic, assign) BECachePolicy cachePolicy;

/**
 The age after which a cached value will be ignored
 */
@property (nonatomic, assign) NSTimeInterval maxCacheAge;

/**
 Returns whether there is a cached result for this query.
 
 @result `YES` if there is a cached result for this query, otherwise `NO`.
 */
@property (nonatomic, assign, readonly) BOOL hasCachedResult;

/**
 Clears the cached result for this query. If there is no cached result, this is a noop.
 */
- (void)clearCachedResult;

/**
 Clears the cached results for all queries.
 */
+ (void)clearAllCachedResults;
@end
