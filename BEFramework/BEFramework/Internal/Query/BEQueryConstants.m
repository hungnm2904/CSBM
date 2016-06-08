//
//  BEQueryConstants.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEQueryConstants.h"

NSString *const BEQueryKeyNotEqualTo = @"$ne";
NSString *const BEQueryKeyLessThan = @"$lt";
NSString *const BEQueryKeyLessThanEqualTo = @"$lte";
NSString *const BEQueryKeyGreaterThan = @"$gt";
NSString *const BEQueryKeyGreaterThanOrEqualTo = @"$gte";
NSString *const BEQueryKeyContainedIn = @"$in";
NSString *const BEQueryKeyNotContainedIn = @"$nin";
NSString *const BEQueryKeyContainsAll = @"$all";
NSString *const BEQueryKeyNearSphere = @"$nearSphere";
NSString *const BEQueryKeyWithin = @"$within";
NSString *const BEQueryKeyRegex = @"$regex";
NSString *const BEQueryKeyExists = @"$exists";
NSString *const BEQueryKeyInQuery = @"$inQuery";
NSString *const BEQueryKeyNotInQuery = @"$notInQuery";
NSString *const BEQueryKeySelect = @"$select";
NSString *const BEQueryKeyDontSelect = @"$dontSelect";
NSString *const BEQueryKeyRelatedTo = @"$relatedTo";
NSString *const BEQueryKeyOr = @"$or";
NSString *const BEQueryKeyQuery = @"query";
NSString *const BEQueryKeyKey = @"key";
NSString *const BEQueryKeyObject = @"object";

NSString *const BEQueryOptionKeyMaxDistance = @"$maxDistance";
NSString *const BEQueryOptionKeyBox = @"$box";
NSString *const BEQueryOptionKeyRegexOptions = @"$options";
