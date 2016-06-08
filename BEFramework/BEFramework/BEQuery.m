//
//  BEQuery.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEQuery.h"
#import "BEQueryPrivate.h"
#import "BEQuery+Synchronous.h"
#import "BEQuery+Deprecated.h"

#import <Bolts/BFCancellationTokenSource.h>
#import <Bolts/BFTask.h>

#import "BFTask+Private.h"
#import "BEAssert.h"
#import "BECommandResult.h"
#import "BECoreManager.h"
#import "BECurrentUserController.h"
#import "BEMutableQueryState.h"
#import "BEObject.h"
#import "BEObjectPrivate.h"
#import "BEQueryController.h"
#import "BEQueryUtilities.h"
#import "BERESTQueryCommand.h"
#import "BEUserPrivate.h"
#import "CSBMInternal.h"
#import "CSBM_Private.h"
#import "BEQueryConstants.h"
#import "CSBM.h"

/**
 Checks if an object can be used as value for query equality clauses.
 */
static void BEQueryAssertValidEqualityClauseClass(id object) {
  static NSArray *classes;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    classes = @[ [NSString class], [NSNumber class], [NSDate class], [NSNull class],
                 [BEObject class] ];
  });
  
  for (Class class in classes) {
    if ([object isKindOfClass:class]) {
      return;
    }
  }
  
  BEParameterAssertionFailure(@"Cannot do a comparison query for type: %@", [object class]);
}

/**
 Checks if an object can be used as value for query ordering clauses.
 */
static void BEQueryAssertValidOrderingClauseClass(id object) {
  static NSArray *classes;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    classes = @[ [NSString class], [NSNumber class], [NSDate class] ];
  });
  
  for (Class class in classes) {
    if ([object isKindOfClass:class]) {
      return;
    }
  }
  
  BEParameterAssertionFailure(@"Cannot do a query that requires ordering for type: %@", [object class]);
}

@interface BEQuery () {
  BFCancellationTokenSource *_cancellationTokenSource;
}

@property (nonatomic, strong, readwrite) BEMutableQueryState *state;

@end

@implementation BEQuery

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithState:(BEQueryState *)state {
  self = [super init];
  if (!self) return nil;
  
  _state = [state mutableCopy];
  
  return self;
}

- (instancetype)initWithClassName:(NSString *)className {
  self = [super init];
  if (!self) return nil;
  
  _state = [BEMutableQueryState stateWithParseClassName:className];
  
  return self;
}

///--------------------------------------
#pragma mark - Public Accessors
///--------------------------------------

#pragma mark Basic

- (NSString *)parseClassName {
  return self.state.parseClassName;
}

- (void)setParseClassName:(NSString *)parseClassName {
  [self checkIfCommandIsRunning];
  self.state.parseClassName = parseClassName;
}

#pragma mark Limit

- (void)setLimit:(NSInteger)limit {
  self.state.limit = limit;
}

- (NSInteger)limit {
  return self.state.limit;
}

#pragma mark Skip

- (void)setSkip:(NSInteger)skip {
  self.state.skip = skip;
}

- (NSInteger)skip {
  return self.state.skip;
}

#pragma mark Cache Policy

- (void)setCachePolicy:(BECachePolicy)cachePolicy {
  //[self _checkPinningEnabled:NO];
  [self checkIfCommandIsRunning];
  
  self.state.cachePolicy = cachePolicy;
}

- (BECachePolicy)cachePolicy {
  //[self _checkPinningEnabled:NO];
  [self checkIfCommandIsRunning];
  
  return self.state.cachePolicy;
}

#pragma mark Cache Policy

- (void)setMaxCacheAge:(NSTimeInterval)maxCacheAge {
  self.state.maxCacheAge = maxCacheAge;
}

- (NSTimeInterval)maxCacheAge {
  return self.state.maxCacheAge;
}

#pragma mark Trace

- (void)setTrace:(BOOL)trace {
  self.state.trace = trace;
}

- (BOOL)trace {
  return self.state.trace;
}

///--------------------------------------
#pragma mark - Order
///--------------------------------------

- (instancetype)orderByAscending:(NSString *)key {
  [self checkIfCommandIsRunning];
  [self.state sortByKey:key ascending:YES];
  return self;
}

- (instancetype)addAscendingOrder:(NSString *)key {
  [self checkIfCommandIsRunning];
  [self.state addSortKey:key ascending:YES];
  return self;
}

- (instancetype)orderByDescending:(NSString *)key {
  [self checkIfCommandIsRunning];
  [self.state sortByKey:key ascending:NO];
  return self;
}

- (instancetype)addDescendingOrder:(NSString *)key {
  [self checkIfCommandIsRunning];
  [self.state addSortKey:key ascending:NO];
  return self;
}

- (instancetype)orderBySortDescriptor:(NSSortDescriptor *)sortDescriptor {
  NSString *key = sortDescriptor.key;
  if (key) {
    if (sortDescriptor.ascending) {
      [self orderByAscending:key];
    } else {
      [self orderByDescending:key];
    }
  }
  return self;
}

- (instancetype)orderBySortDescriptors:(NSArray *)sortDescriptors {
  [self.state addSortKeysFromSortDescriptors:sortDescriptors];
  return self;
}

///--------------------------------------
#pragma mark - Conditions
///--------------------------------------

// Helper for condition queries.
- (instancetype)whereKey:(NSString *)key condition:(NSString *)condition object:(id)object {
  [self checkIfCommandIsRunning];
  [self.state setConditionType:condition withObject:object forKey:key];
  return self;
}

- (instancetype)whereKey:(NSString *)key equalTo:(id)object {
  [self checkIfCommandIsRunning];
  BEQueryAssertValidEqualityClauseClass(object);
  [self.state setEqualityConditionWithObject:object forKey:key];
  return self;
}

- (instancetype)whereKey:(NSString *)key greaterThan:(id)object {
  BEQueryAssertValidOrderingClauseClass(object);
  return [self whereKey:key condition:BEQueryKeyGreaterThan object:object];
}

- (instancetype)whereKey:(NSString *)key greaterThanOrEqualTo:(id)object {
  BEQueryAssertValidOrderingClauseClass(object);
  return [self whereKey:key condition:BEQueryKeyGreaterThanOrEqualTo object:object];
}

- (instancetype)whereKey:(NSString *)key lessThan:(id)object {
  BEQueryAssertValidOrderingClauseClass(object);
  return [self whereKey:key condition:BEQueryKeyLessThan object:object];
}

- (instancetype)whereKey:(NSString *)key lessThanOrEqualTo:(id)object {
  BEQueryAssertValidOrderingClauseClass(object);
  return [self whereKey:key condition:BEQueryKeyLessThanEqualTo object:object];
}

- (instancetype)whereKey:(NSString *)key notEqualTo:(id)object {
  BEQueryAssertValidEqualityClauseClass(object);
  return [self whereKey:key condition:BEQueryKeyNotEqualTo object:object];
}

- (instancetype)whereKey:(NSString *)key containedIn:(NSArray *)inArray {
  return [self whereKey:key condition:BEQueryKeyContainedIn object:inArray];
}

- (instancetype)whereKey:(NSString *)key notContainedIn:(NSArray *)inArray {
  return [self whereKey:key condition:BEQueryKeyNotContainedIn object:inArray];
}

- (instancetype)whereKey:(NSString *)key containsAllObjectsInArray:(NSArray *)array {
  return [self whereKey:key condition:BEQueryKeyContainsAll object:array];
}

//- (instancetype)whereKey:(NSString *)key nearGeoPoint:(BEGeoPoint *)geopoint {
//  return [self whereKey:key condition:BEQueryKeyNearSphere object:geopoint];
//}
//
//- (instancetype)whereKey:(NSString *)key nearGeoPoint:(BEGeoPoint *)geopoint withinRadians:(double)maxDistance {
//  return [[self whereKey:key condition:BEQueryKeyNearSphere object:geopoint]
//          whereKey:key condition:BEQueryOptionKeyMaxDistance object:@(maxDistance)];
//}
//
//- (instancetype)whereKey:(NSString *)key nearGeoPoint:(BEGeoPoint *)geopoint withinMiles:(double)maxDistance {
//  return [self whereKey:key nearGeoPoint:geopoint withinRadians:(maxDistance / EARTH_RADIUS_MILES)];
//}
//
//- (instancetype)whereKey:(NSString *)key nearGeoPoint:(BEGeoPoint *)geopoint withinKilometers:(double)maxDistance {
//  return [self whereKey:key nearGeoPoint:geopoint withinRadians:(maxDistance / EARTH_RADIUS_KILOMETERS)];
//}
//
//- (instancetype)whereKey:(NSString *)key withinGeoBoxFromSouthwest:(BEGeoPoint *)southwest toNortheast:(BEGeoPoint *)northeast {
//  NSArray *array = @[ southwest, northeast ];
//  NSDictionary *dictionary = @{ BEQueryOptionKeyBox : array };
//  return [self whereKey:key condition:BEQueryKeyWithin object:dictionary];
//}

- (instancetype)whereKey:(NSString *)key matchesRegex:(NSString *)regex {
  return [self whereKey:key condition:BEQueryKeyRegex object:regex];
}

- (instancetype)whereKey:(NSString *)key matchesRegex:(NSString *)regex modifiers:(NSString *)modifiers {
  [self checkIfCommandIsRunning];
  NSMutableDictionary *dictionary = [NSMutableDictionary dictionaryWithCapacity:2];
  dictionary[BEQueryKeyRegex] = regex;
  if (modifiers.length) {
    dictionary[BEQueryOptionKeyRegexOptions] = modifiers;
  }
  [self.state setEqualityConditionWithObject:dictionary forKey:key];
  return self;
}

- (instancetype)whereKey:(NSString *)key containsString:(NSString *)substring {
  NSString *regex = [BEQueryUtilities regexStringForString:substring];
  return [self whereKey:key matchesRegex:regex];
}

- (instancetype)whereKey:(NSString *)key hasPrefix:(NSString *)prefix {
  NSString *regex = [NSString stringWithFormat:@"^%@", [BEQueryUtilities regexStringForString:prefix]];
  return [self whereKey:key matchesRegex:regex];
}

- (instancetype)whereKey:(NSString *)key hasSuffix:(NSString *)suffix {
  NSString *regex = [NSString stringWithFormat:@"%@$", [BEQueryUtilities regexStringForString:suffix]];
  return [self whereKey:key matchesRegex:regex];
}

- (instancetype)whereKeyExists:(NSString *)key {
  return [self whereKey:key condition:BEQueryKeyExists object:@YES];
}

- (instancetype)whereKeyDoesNotExist:(NSString *)key {
  return [self whereKey:key condition:BEQueryKeyExists object:@NO];
}

- (instancetype)whereKey:(NSString *)key matchesQuery:(BEQuery *)query {
  return [self whereKey:key condition:BEQueryKeyInQuery object:query];
}

- (instancetype)whereKey:(NSString *)key doesNotMatchQuery:(BEQuery *)query {
  return [self whereKey:key condition:BEQueryKeyNotInQuery object:query];
}

- (instancetype)whereKey:(NSString *)key matchesKey:(NSString *)otherKey inQuery:(BEQuery *)query {
  NSMutableDictionary *dict = [NSMutableDictionary dictionaryWithCapacity:2];
  dict[BEQueryKeyQuery] = query;
  dict[BEQueryKeyKey] = otherKey;
  return [self whereKey:key condition:BEQueryKeySelect object:dict];
}

- (instancetype)whereKey:(NSString *)key doesNotMatchKey:(NSString *)otherKey inQuery:(BEQuery *)query {
  NSMutableDictionary *dict = [NSMutableDictionary dictionaryWithCapacity:2];
  dict[BEQueryKeyQuery] = query;
  dict[BEQueryKeyKey] = otherKey;
  return [self whereKey:key condition:BEQueryKeyDontSelect object:dict];
}

- (instancetype)whereRelatedToObject:(BEObject *)parent fromKey:(NSString *)key {
  [self.state setRelationConditionWithObject:parent forKey:key];
  return self;
}

- (void)redirectClassNameForKey:(NSString *)key {
  [self.state redirectClassNameForKey:key];
}

///--------------------------------------
#pragma mark - Include
///--------------------------------------

- (instancetype)includeKey:(NSString *)key {
  [self checkIfCommandIsRunning];
  [self.state includeKey:key];
  return self;
}

- (instancetype)includeKeys:(NSArray<NSString *> *)keys {
  [self checkIfCommandIsRunning];
  [self.state includeKeys:keys];
  return self;
}

///--------------------------------------
#pragma mark - Select
///--------------------------------------

- (instancetype)selectKeys:(NSArray *)keys {
  [self checkIfCommandIsRunning];
  [self.state selectKeys:keys];
  return self;
}

///--------------------------------------
#pragma mark - NSPredicate helper methods
///--------------------------------------

+ (void)assertKeyPathConstant:(NSComparisonPredicate *)predicate {
  BEConsistencyAssert(predicate.leftExpression.expressionType == NSKeyPathExpressionType &&
                      predicate.rightExpression.expressionType == NSConstantValueExpressionType,
                      @"This predicate must have a key path and a constant. %@", predicate);
}

// Adds the conditions from an NSComparisonPredicate to a BEQuery.
- (void)whereComparisonPredicate:(NSComparisonPredicate *)predicate {
  NSExpression *left = predicate.leftExpression;
  NSExpression *right = predicate.rightExpression;
  
  switch (predicate.predicateOperatorType) {
      case NSEqualToPredicateOperatorType: {
        [[self class] assertKeyPathConstant:predicate];
        [self whereKey:left.keyPath equalTo:(right.constantValue ?: [NSNull null])];
        return;
      }
      case NSNotEqualToPredicateOperatorType: {
        [[self class] assertKeyPathConstant:predicate];
        [self whereKey:left.keyPath notEqualTo:(right.constantValue ?: [NSNull null])];
        return;
      }
      case NSLessThanPredicateOperatorType: {
        [[self class] assertKeyPathConstant:predicate];
        [self whereKey:left.keyPath lessThan:right.constantValue];
        return;
      }
      case NSLessThanOrEqualToPredicateOperatorType: {
        [[self class] assertKeyPathConstant:predicate];
        [self whereKey:left.keyPath lessThanOrEqualTo:right.constantValue];
        return;
      }
      case NSGreaterThanPredicateOperatorType: {
        [[self class] assertKeyPathConstant:predicate];
        [self whereKey:left.keyPath greaterThan:right.constantValue];
        return;
      }
      case NSGreaterThanOrEqualToPredicateOperatorType: {
        [[self class] assertKeyPathConstant:predicate];
        [self whereKey:left.keyPath greaterThanOrEqualTo:right.constantValue];
        return;
      }
      case NSInPredicateOperatorType: {
        if (left.expressionType == NSKeyPathExpressionType &&
            right.expressionType == NSConstantValueExpressionType) {
          if ([right.constantValue isKindOfClass:[BEQuery class]]) {
            // Like "value IN subquery
            [self whereKey:left.keyPath matchesQuery:right.constantValue];
          } else {
            // Like "value IN %@", @{@1, @2, @3, @4}
            [self whereKey:left.keyPath containedIn:right.constantValue];
          }
        } else if (left.expressionType == NSKeyPathExpressionType &&
                   right.expressionType == NSAggregateExpressionType &&
                   [right.constantValue isKindOfClass:[NSArray class]]) {
          // Like "value IN {1, 2, 3, 4}"
          NSArray *constants = right.constantValue;
          NSMutableArray *values = [NSMutableArray arrayWithCapacity:constants.count];
          for (NSExpression *expression in constants) {
            [values addObject:expression.constantValue];
          }
          [self whereKey:left.keyPath containedIn:values];
        } else if (right.expressionType == NSEvaluatedObjectExpressionType &&
                   left.expressionType == NSKeyPathExpressionType) {
          // Like "value IN SELF"
          [self whereKeyExists:left.keyPath];
        } else {
          BEConsistencyAssertionFailure(@"An IN predicate must have a key path and a constant.");
        }
        return;
      }
      case NSCustomSelectorPredicateOperatorType: {
        if (predicate.customSelector != NSSelectorFromString(@"notContainedIn:")) {
          BEConsistencyAssertionFailure(@"Predicates with custom selectors are not supported.");
        }
        
        if (right.expressionType == NSConstantValueExpressionType &&
            left.expressionType == NSKeyPathExpressionType) {
          if ([right.constantValue isKindOfClass:[BEQuery class]]) {
            // Like "NOT (value IN subquery)"
            [self whereKey:left.keyPath doesNotMatchQuery:right.constantValue];
          } else {
            // Like "NOT (value in %@)", @{@1, @2, @3}
            [self whereKey:left.keyPath notContainedIn:right.constantValue];
          }
        } else if (left.expressionType == NSKeyPathExpressionType &&
                   right.expressionType == NSAggregateExpressionType &&
                   [right.constantValue isKindOfClass:[NSArray class]]) {
          // Like "NOT (value IN {1, 2, 3, 4})"
          NSArray *constants = right.constantValue;
          NSMutableArray *values = [NSMutableArray arrayWithCapacity:constants.count];
          for (NSExpression *expression in constants) {
            [values addObject:expression.constantValue];
          }
          [self whereKey:left.keyPath notContainedIn:values];
        } else if (right.expressionType == NSEvaluatedObjectExpressionType &&
                   left.expressionType == NSKeyPathExpressionType) {
          // Like "NOT (value IN SELF)"
          [self whereKeyDoesNotExist:left.keyPath];
        } else {
          BEConsistencyAssertionFailure(@"A NOT IN predicate must have a key path and a constant array.");
        }
        return;
      }
      case NSBeginsWithPredicateOperatorType: {
        [[self class] assertKeyPathConstant:predicate];
        [self whereKey:left.keyPath hasPrefix:right.constantValue];
        return;
      }
      case NSContainsPredicateOperatorType:
      case NSEndsWithPredicateOperatorType:
      case NSMatchesPredicateOperatorType: {
        BEConsistencyAssertionFailure(@"Regex queries are not supported with "
                                      "[BEQuery queryWithClassName:predicate:]. Please try to structure your "
                                      "data so that you can use an equalTo or containedIn query.");
      }
      case NSLikePredicateOperatorType: {
        BEConsistencyAssertionFailure(@"LIKE is not supported by BEQuery.");
      }
      case NSBetweenPredicateOperatorType:
    default: {
      BEConsistencyAssertionFailure(@"This comparison predicate is not supported. (%zd)", predicate.predicateOperatorType);
    }
  }
}

/**
 Creates a BEQuery with the constraints given by predicate.
 This method assumes the predicate has already been normalized.
 */
+ (instancetype)queryWithClassName:(NSString *)className normalizedPredicate:(NSPredicate *)predicate {
  if ([predicate isKindOfClass:[NSComparisonPredicate class]]) {
    BEQuery *query = [self queryWithClassName:className];
    [query whereComparisonPredicate:(NSComparisonPredicate *)predicate];
    return query;
  } else if ([predicate isKindOfClass:[NSCompoundPredicate class]]) {
    NSCompoundPredicate *compound = (NSCompoundPredicate *)predicate;
    switch (compound.compoundPredicateType) {
        case NSAndPredicateType: {
          BEQuery *query = nil;
          NSMutableArray *subpredicates = [NSMutableArray array];
          // If there's an OR query in here, we'll start with it.
          for (NSPredicate *subpredicate in compound.subpredicates) {
            if ([subpredicate isKindOfClass:[NSCompoundPredicate class]] &&
                ((NSCompoundPredicate *)subpredicate).compoundPredicateType == NSOrPredicateType) {
              if (query) {
                BEConsistencyAssertionFailure(@"A query had 2 ORs in an AND after normalization. %@", predicate);
              }
              query = [self queryWithClassName:className normalizedPredicate:subpredicate];
            } else {
              [subpredicates addObject:subpredicate];
            }
          }
          // If there was no OR query, then start with an empty query.
          if (!query) {
            query = [self queryWithClassName:className];
          }
          for (NSPredicate *subpredicate in subpredicates) {
            if (![subpredicate isKindOfClass:[NSComparisonPredicate class]]) {
              // This should never happen.
              BEConsistencyAssertionFailure(@"A predicate had a non-comparison predicate inside an AND after normalization. %@",
                                            predicate);
            }
            NSComparisonPredicate *comparison = (NSComparisonPredicate *)subpredicate;
            [query whereComparisonPredicate:comparison];
          }
          return query;
        }
        case NSOrPredicateType: {
          NSMutableArray *subqueries = [NSMutableArray arrayWithCapacity:compound.subpredicates.count];
          if (compound.subpredicates.count > 4) {
            BEConsistencyAssertionFailure(@"This query is too complex. It had an OR with >4 subpredicates after normalization.");
          }
          for (NSPredicate *subpredicate in compound.subpredicates) {
            [subqueries addObject:[self queryWithClassName:className normalizedPredicate:subpredicate]];
          }
          return [self orQueryWithSubqueries:subqueries];
        }
        case NSNotPredicateType:
      default: {
        // This should never happen.
        BEConsistencyAssertionFailure(@"A predicate had a NOT after normalization. %@", predicate);
        return nil;
      }
    }
  } else {
    BEConsistencyAssertionFailure(@"Unknown predicate type.");
    return nil;
  }
}

///--------------------------------------
#pragma mark - Helpers
///--------------------------------------

- (void)checkIfCommandIsRunning {
  @synchronized(self) {
    if (_cancellationTokenSource) {
      BEConsistencyAssertionFailure(@"This query has an outstanding network connection. You have to wait until it's done.");
    }
  }
}

- (void)markAsRunning:(BFCancellationTokenSource *)source {
  [self checkIfCommandIsRunning];
  @synchronized(self) {
    _cancellationTokenSource = source;
  }
}

///--------------------------------------
#pragma mark - Constructors
///--------------------------------------

+ (instancetype)queryWithClassName:(NSString *)className {
  return [[self alloc] initWithClassName:className];
}

+ (instancetype)queryWithClassName:(NSString *)className predicate:(NSPredicate *)predicate {
  if (!predicate) {
    return [self queryWithClassName:className];
  }
  
  NSPredicate *normalizedPredicate = [BEQueryUtilities predicateByNormalizingPredicate:predicate];
  return [self queryWithClassName:className normalizedPredicate:normalizedPredicate];
}

+ (instancetype)orQueryWithSubqueries:(NSArray<BEQuery *> *)queries {
  BEParameterAssert(queries.count, @"Can't create an `or` query from no subqueries.");
  NSMutableArray *array = [NSMutableArray arrayWithCapacity:queries.count];
  NSString *className = queries.firstObject.parseClassName;
  for (BEQuery *query in queries) {
    BEParameterAssert([query isKindOfClass:[BEQuery class]],
                      @"All elements should be instances of `BEQuery` class.");
    BEParameterAssert([query.parseClassName isEqualToString:className],
                      @"All sub queries of an `or` query should be on the same class.");
    
    [array addObject:query];
  }
  BEQuery *query = [self queryWithClassName:className];
  [query.state setEqualityConditionWithObject:array forKey:BEQueryKeyOr];
  return query;
}

///--------------------------------------
#pragma mark - Get with objectId
///--------------------------------------

//- (BFTask *)getObjectInBackgroundWithId:(NSString *)objectId {
//  if (objectId.length == 0) {
//    return [BFTask taskWithResult:nil];
//  }
//  
//  BEConsistencyAssert(self.state.cachePolicy != kBECachePolicyCacheThenNetwork,
//                      @"kBECachePolicyCacheThenNetwork can only be used with methods that have a callback.");
//  return [self _getObjectWithIdAsync:objectId cachePolicy:self.state.cachePolicy after:nil];
//}
//
//- (void)getObjectInBackgroundWithId:(NSString *)objectId block:(BEObjectResultBlock)block {
//  @synchronized(self) {
//    if (!self.state.queriesLocalDatastore && self.state.cachePolicy == kBECachePolicyCacheThenNetwork) {
//      BFTask *cacheTask = [[self _getObjectWithIdAsync:objectId
//                                           cachePolicy:kBECachePolicyCacheOnly
//                                                 after:nil] thenCallBackOnMainThreadAsync:block];
//      [[self _getObjectWithIdAsync:objectId
//                       cachePolicy:kBECachePolicyNetworkOnly
//                             after:cacheTask] thenCallBackOnMainThreadAsync:block];
//    } else {
//      [[self getObjectInBackgroundWithId:objectId] thenCallBackOnMainThreadAsync:block];
//    }
//  }
//}

//- (BFTask *)_getObjectWithIdAsync:(NSString *)objectId cachePolicy:(BECachePolicy)cachePolicy after:(BFTask *)task {
//  self.limit = 1;
//  self.skip = 0;
//  [self.state removeAllConditions];
//  [self.state setEqualityConditionWithObject:objectId forKey:@"objectId"];
//  
//  BEQueryState *state = [self _queryStateCopyWithCachePolicy:cachePolicy];
//  return [[self _findObjectsAsyncForQueryState:state
//                                         after:task] continueWithSuccessBlock:^id(BFTask *task) {
//    NSArray *objects = task.result;
//    if (objects.count == 0) {
//      return [BFTask taskWithError:[BEQueryUtilities objectNotFoundError]];
//    }
//    
//    return objects.lastObject;
//  }];
//}

///--------------------------------------
#pragma mark - Get Users (Deprecated)
///--------------------------------------

+ (instancetype)queryForUser {
  return [BEUser query];
}

///--------------------------------------
#pragma mark - Find Objects
///--------------------------------------

//- (BFTask *)findObjectsInBackground {
//  BEQueryState *state = [self _queryStateCopy];
//  
//  BEConsistencyAssert(state.cachePolicy != kBECachePolicyCacheThenNetwork,
//                      @"kBECachePolicyCacheThenNetwork can only be used with methods that have a callback.");
//  return [self _findObjectsAsyncForQueryState:state after:nil];
//}
//
//- (void)findObjectsInBackgroundWithBlock:(BEQueryArrayResultBlock)block {
//  @synchronized(self) {
//    if (!self.state.queriesLocalDatastore && self.state.cachePolicy == kBECachePolicyCacheThenNetwork) {
//      BEQueryState *cacheQueryState = [self _queryStateCopyWithCachePolicy:kBECachePolicyCacheOnly];
//      BFTask *cacheTask = [[self _findObjectsAsyncForQueryState:cacheQueryState
//                                                          after:nil] thenCallBackOnMainThreadAsync:block];
//      
//      BEQueryState *remoteQueryState = [self _queryStateCopyWithCachePolicy:kBECachePolicyNetworkOnly];
//      [[self _findObjectsAsyncForQueryState:remoteQueryState
//                                      after:cacheTask] thenCallBackOnMainThreadAsync:block];
//    } else {
//      [[self findObjectsInBackground] thenCallBackOnMainThreadAsync:block];
//    }
//  }
//}
//
//- (BFTask *)_findObjectsAsyncForQueryState:(BEQueryState *)queryState after:(BFTask *)previous {
//  BFCancellationTokenSource *cancellationTokenSource = _cancellationTokenSource;
//  if (!previous) {
//    cancellationTokenSource = [BFCancellationTokenSource cancellationTokenSource];
//    [self markAsRunning:cancellationTokenSource];
//  }
//  
//  BFTask *start = (previous ?: [BFTask taskWithResult:nil]);
//  
//  [self _validateQueryState];
//  @weakify(self);
//  return [[[start continueWithBlock:^id(BFTask *task) {
//    @strongify(self);
//    return [[self class] _getCurrentUserForQueryState:queryState];
//  }] continueWithBlock:^id(BFTask *task) {
//    @strongify(self);
//    BEUser *user = task.result;
//    return [[[self class] queryController] findObjectsAsyncForQueryState:queryState
//                                                   withCancellationToken:cancellationTokenSource.token
//                                                                    user:user];
//  }] continueWithBlock:^id(BFTask *task) {
//    @strongify(self);
//    if (!self) {
//      return task;
//    }
//    @synchronized (self) {
//      if (_cancellationTokenSource == cancellationTokenSource) {
//        _cancellationTokenSource = nil;
//      }
//    }
//    return task;
//  }];
//}

///--------------------------------------
#pragma mark - Get Object
///--------------------------------------

//- (BFTask *)getFirstObjectInBackground {
//  BEConsistencyAssert(self.state.cachePolicy != kBECachePolicyCacheThenNetwork,
//                      @"kBECachePolicyCacheThenNetwork can only be used with methods that have a callback.");
//  return [self _getFirstObjectAsyncWithCachePolicy:self.state.cachePolicy after:nil];
//}
//
//- (void)getFirstObjectInBackgroundWithBlock:(BEObjectResultBlock)block {
//  @synchronized(self) {
//    if (!self.state.queriesLocalDatastore && self.state.cachePolicy == kBECachePolicyCacheThenNetwork) {
//      BFTask *cacheTask = [[self _getFirstObjectAsyncWithCachePolicy:kBECachePolicyCacheOnly
//                                                               after:nil] thenCallBackOnMainThreadAsync:block];
//      [[self _getFirstObjectAsyncWithCachePolicy:kBECachePolicyNetworkOnly
//                                           after:cacheTask] thenCallBackOnMainThreadAsync:block];
//    } else {
//      [[self getFirstObjectInBackground] thenCallBackOnMainThreadAsync:block];
//    }
//  }
//}

//- (BFTask *)_getFirstObjectAsyncWithCachePolicy:(BECachePolicy)cachePolicy after:(BFTask *)task {
//  self.limit = 1;
//  
//  BEQueryState *state = [self _queryStateCopyWithCachePolicy:cachePolicy];
//  return [[self _findObjectsAsyncForQueryState:state after:task] continueWithSuccessBlock:^id(BFTask *task) {
//    NSArray *objects = task.result;
//    if (objects.count == 0) {
//      return [BFTask taskWithError:[BEQueryUtilities objectNotFoundError]];
//    }
//    
//    return objects.lastObject;
//  }];
//}

///--------------------------------------
#pragma mark - Count Objects
///--------------------------------------

//- (BFTask *)countObjectsInBackground {
//  BEConsistencyAssert(self.state.cachePolicy != kBECachePolicyCacheThenNetwork,
//                      @"kBECachePolicyCacheThenNetwork can only be used with methods that have a callback.");
//  return [self _countObjectsAsyncForQueryState:[self _queryStateCopy] after:nil];
//}

//- (void)countObjectsInBackgroundWithBlock:(BEIntegerResultBlock)block {
//  BEIdResultBlock callback = nil;
//  if (block) {
//    callback = ^(id result, NSError *error) {
//      block([result intValue], error);
//    };
//  }
//  
//  @synchronized(self) {
//    if (!self.state.queriesLocalDatastore && self.state.cachePolicy == kBECachePolicyCacheThenNetwork) {
//      BEQueryState *cacheQueryState = [self _queryStateCopyWithCachePolicy:kBECachePolicyCacheOnly];
//      BFTask *cacheTask = [[self _countObjectsAsyncForQueryState:cacheQueryState
//                                                           after:nil] thenCallBackOnMainThreadAsync:callback];
//      
//      BEQueryState *remoteQueryState = [self _queryStateCopyWithCachePolicy:kBECachePolicyNetworkOnly];
//      [[self _countObjectsAsyncForQueryState:remoteQueryState
//                                       after:cacheTask] thenCallBackOnMainThreadAsync:callback];
//    } else {
//      [[self countObjectsInBackground] thenCallBackOnMainThreadAsync:callback];
//    }
//  }
//}

//- (BFTask *)_countObjectsAsyncForQueryState:(BEQueryState *)queryState after:(BFTask *)previousTask {
//  BFCancellationTokenSource *cancellationTokenSource = _cancellationTokenSource;
//  if (!previousTask) {
//    cancellationTokenSource = [BFCancellationTokenSource cancellationTokenSource];
//    [self markAsRunning:cancellationTokenSource];
//  }
//  
//  BFTask *start = (previousTask ?: [BFTask taskWithResult:nil]);
//  
//  [self _validateQueryState];
//  @weakify(self);
//  return [[[start continueWithBlock:^id(BFTask *task) {
//    return [[self class] _getCurrentUserForQueryState:queryState];
//  }] continueWithBlock:^id(BFTask *task) {
//    @strongify(self);
//    BEUser *user = task.result;
//    return [[[self class] queryController] countObjectsAsyncForQueryState:queryState
//                                                    withCancellationToken:cancellationTokenSource.token
//                                                                     user:user];
//  }] continueWithBlock:^id(BFTask *task) {
//    @synchronized(self) {
//      if (_cancellationTokenSource == cancellationTokenSource) {
//        _cancellationTokenSource = nil;
//      }
//    }
//    return task;
//  }];
//}

///--------------------------------------
#pragma mark - Cancel
///--------------------------------------

- (void)cancel {
  @synchronized(self) {
    if (_cancellationTokenSource) {
      [_cancellationTokenSource cancel];
      _cancellationTokenSource = nil;
    }
  }
}

///--------------------------------------
#pragma mark - NSCopying
///--------------------------------------

- (instancetype)copyWithZone:(NSZone *)zone {
  return [[[self class] allocWithZone:zone] initWithState:self.state];
}

///--------------------------------------
#pragma mark NSObject
///--------------------------------------

- (NSUInteger)hash {
  return self.state.hash;
}

- (BOOL)isEqual:(id)object {
  if (self == object) {
    return YES;
  }
  
  if (![object isKindOfClass:[BEQuery class]]) {
    return NO;
  }
  
  return [self.state isEqual:((BEQuery *)object).state];
}

///--------------------------------------
#pragma mark - Caching
///--------------------------------------

- (BOOL)hasCachedResult {
  return [[[self class] queryController] hasCachedResultForQueryState:self.state
                                                         sessionToken:[BEUser currentSessionToken]];
}

- (void)clearCachedResult {
  [[[self class] queryController] clearCachedResultForQueryState:self.state
                                                    sessionToken:[BEUser currentSessionToken]];
}

//+ (void)clearAllCachedResults {
//  [[self queryController] clearAllCachedResults];
//}

///--------------------------------------
#pragma mark - Check Pinning Status
///--------------------------------------

/**
 If `enabled` is YES, raise an exception if OfflineStore is not enabled. If `enabled` is NO, raise
 an exception if OfflineStore is enabled.
 */
//- (void)_checkPinningEnabled:(BOOL)enabled {
//  BOOL loaded = [Parse _currentManager].offlineStoreLoaded;
//  if (enabled) {
//    BEConsistencyAssert(loaded, @"Method requires Pinning enabled.");
//  } else {
//    BEConsistencyAssert(!loaded, @"Method not allowed when Pinning is enabled.");
//  }
//}
//
/////--------------------------------------
//#pragma mark - Query Source
/////--------------------------------------
//
//- (instancetype)fromLocalDatastore {
//  return [self fromPinWithName:nil];
//}
//
//- (instancetype)fromPin {
//  return [self fromPinWithName:BEObjectDefaultPin];
//}
//
//- (instancetype)fromPinWithName:(NSString *)name {
//  [self _checkPinningEnabled:YES];
//  [self checkIfCommandIsRunning];
//  
//  self.state.queriesLocalDatastore = YES;
//  self.state.localDatastorePinName = [name copy];
//  
//  return self;
//}
//
//- (instancetype)ignoreACLs {
//  [self _checkPinningEnabled:YES];
//  [self checkIfCommandIsRunning];
//  
//  self.state.shouldIgnoreACLs = YES;
//  
//  return self;
//}

///--------------------------------------
#pragma mark - Query State
///--------------------------------------

- (BEQueryState *)_queryStateCopy {
  return [self.state copy];
}

- (BEQueryState *)_queryStateCopyWithCachePolicy:(BECachePolicy)cachePolicy {
  BEMutableQueryState *state = [self.state mutableCopy];
  state.cachePolicy = cachePolicy;
  return state;
}

- (void)_validateQueryState {
  BEConsistencyAssert(self.state.queriesLocalDatastore || !self.state.shouldIgnoreACLs,
                      @"`ignoreACLs` can only be used with Local Datastore queries.");
}

///--------------------------------------
#pragma mark - Query Controller
///--------------------------------------

//+ (BEQueryController *)queryController {
//  return [CSBM _currentManager].coreManager.queryController;
//}
//
/////--------------------------------------
//#pragma mark - User
/////--------------------------------------
//
//+ (BFTask *)_getCurrentUserForQueryState:(BEQueryState *)state {
//  if (state.shouldIgnoreACLs) {
//    return [BFTask taskWithResult:nil];
//  }
//  return [[CSBM _currentManager].coreManager.currentUserController getCurrentObjectAsync];
//}

@end

///--------------------------------------
#pragma mark - Synchronous
///--------------------------------------

@implementation BEQuery (Synchronous)

#pragma mark Getting Objects by ID

+ (BEObject *)getObjectOfClass:(NSString *)objectClass objectId:(NSString *)objectId {
  return [self getObjectOfClass:objectClass objectId:objectId error:nil];
}

+ (BEObject *)getObjectOfClass:(NSString *)objectClass objectId:(NSString *)objectId error:(NSError **)error {
  BEQuery *query = [self queryWithClassName:objectClass];
  return [query getObjectWithId:objectId error:error];
}

- (BEObject *)getObjectWithId:(NSString *)objectId {
  return [self getObjectWithId:objectId error:nil];
}

- (BEObject *)getObjectWithId:(NSString *)objectId error:(NSError **)error {
  return [[self getObjectInBackgroundWithId:objectId] waitForResult:error];
}

#pragma mark Getting User Objects

+ (BEUser *)getUserObjectWithId:(NSString *)objectId {
  return [self getUserObjectWithId:objectId error:nil];
}

+ (BEUser *)getUserObjectWithId:(NSString *)objectId error:(NSError **)error {
  BEQuery *query = [BEUser query];
  return [query getObjectWithId:objectId error:error];
}

#pragma mark Getting all Matches for a Query

- (NSArray *)findObjects {
  return [self findObjects:nil];
}

- (NSArray *)findObjects:(NSError **)error {
  return [[self findObjectsInBackground] waitForResult:error];
}

#pragma mark Getting First Match in a Query

- (BEObject *)getFirstObject {
  return [self getFirstObject:nil];
}

- (BEObject *)getFirstObject:(NSError **)error {
  return [[self getFirstObjectInBackground] waitForResult:error];
}

#pragma mark Counting the Matches in a Query

- (NSInteger)countObjects {
  return [self countObjects:nil];
}

- (NSInteger)countObjects:(NSError **)error {
  NSNumber *count = [[self countObjectsInBackground] waitForResult:error];
  if (!count) {
    // TODO: (nlutsenko) It's really weird that we are inconsistent in sync vs async methods.
    // Leaving for now since some devs might be relying on this.
    return -1;
  }
  
  return count.integerValue;
}

@end

///--------------------------------------
#pragma mark - Deprecated
///--------------------------------------

@implementation BEQuery (Deprecated)

#pragma mark Getting Objects by ID

- (void)getObjectInBackgroundWithId:(NSString *)objectId target:(nullable id)target selector:(nullable SEL)selector {
  [self getObjectInBackgroundWithId:objectId block:^(BEObject *object, NSError *error) {
    [BEInternalUtils safePerformSelector:selector withTarget:target object:object object:error];
  }];
}

#pragma mark Getting all Matches for a Query

- (void)findObjectsInBackgroundWithTarget:(nullable id)target selector:(nullable SEL)selector {
  [self findObjectsInBackgroundWithBlock:^(NSArray *objects, NSError *error) {
    [BEInternalUtils safePerformSelector:selector withTarget:target object:objects object:error];
  }];
}

#pragma mark Getting the First Match in a Query

- (void)getFirstObjectInBackgroundWithTarget:(nullable id)target selector:(nullable SEL)selector {
  [self getFirstObjectInBackgroundWithBlock:^(BEObject *result, NSError *error) {
    [BEInternalUtils safePerformSelector:selector withTarget:target object:result object:error];
  }];
}

#pragma mark Counting the Matches in a Query

- (void)countObjectsInBackgroundWithTarget:(nullable id)target selector:(nullable SEL)selector {
  [self countObjectsInBackgroundWithBlock:^(int number, NSError *error) {
    [BEInternalUtils safePerformSelector:selector withTarget:target object:@(number) object:error];
  }];
}

@end
