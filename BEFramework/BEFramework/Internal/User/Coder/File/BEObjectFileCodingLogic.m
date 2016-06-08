//
//  BEObjectFileCodingLogic.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEObjectFileCodingLogic.h"

#import "BEMutableObjectState.h"
#import "BEObjectPrivate.h"

@implementation BEObjectFileCodingLogic

///--------------------------------------
#pragma mark - Init
///--------------------------------------

+ (instancetype)codingLogic {
  return [[self alloc] init];
}

///--------------------------------------
#pragma mark - Logic
///--------------------------------------

- (void)updateObject:(BEObject *)object fromDictionary:(NSDictionary *)dictionary usingDecoder:(BEDecoder *)decoder {
  object._state = [object._state copyByMutatingWithBlock:^(BEMutableObjectState *state) {
    NSString *newObjectId = dictionary[@"id"];
    if (newObjectId) {
      state.objectId = newObjectId;
    }
    NSString *createdAtString = dictionary[@"created_at"];
    if (createdAtString) {
      [state setCreatedAtFromString:createdAtString];
    }
    NSString *updatedAtString = dictionary[@"updated_at"];
    if (updatedAtString) {
      [state setUpdatedAtFromString:updatedAtString];
    }
  }];
  
  NSDictionary *newPointers = dictionary[@"pointers"];
  NSMutableDictionary *pointersDictionary = [NSMutableDictionary dictionaryWithCapacity:newPointers.count];
  [newPointers enumerateKeysAndObjectsUsingBlock:^(id key, NSArray *pointerArray, BOOL *stop) {
    BEObject *pointer = [BEObject objectWithoutDataWithClassName:pointerArray.firstObject
                                                        objectId:pointerArray.lastObject];
    pointersDictionary[key] = pointer;
  }];
  
  NSMutableDictionary *dataDictionary = [NSMutableDictionary dictionaryWithDictionary:dictionary[@"data"]];
  [dataDictionary addEntriesFromDictionary:pointersDictionary];
  [object _mergeAfterFetchWithResult:dataDictionary decoder:decoder completeData:YES];
}


@end
