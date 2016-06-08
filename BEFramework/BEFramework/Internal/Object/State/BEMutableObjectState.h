//
//  BEMutableObjectState.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEObjectState.h"
#import "BEOperationSet.h"

@interface BEMutableObjectState : BEObjectState

@property (nonatomic, copy, readwrite) NSString *csbmClassName;
@property (nonatomic, copy, readwrite) NSString *objectId;

@property (nonatomic, strong, readwrite) NSDate *createdAt;
@property (nonatomic, strong, readwrite) NSDate *updatedAt;

@property (nonatomic, copy, readwrite) NSDictionary *serverData;

@property (nonatomic, assign, readwrite, getter=isComplete) BOOL complete;
@property (nonatomic, assign, readwrite, getter=isDeleted) BOOL deleted;

///--------------------------------------
#pragma mark - Accessors
///--------------------------------------

- (void)setServerDataObject:(id)object forKey:(NSString *)key;
- (void)removeServerDataObjectForKey:(NSString *)key;
- (void)removeServerDataObjectsForKeys:(NSArray *)keys;

- (void)setCreatedAtFromString:(NSString *)string;
- (void)setUpdatedAtFromString:(NSString *)string;

///--------------------------------------
#pragma mark - Apply
///--------------------------------------

- (void)applyState:(BEObjectState *)state;
- (void)applyOperationSet:(BEOperationSet *)operationSet;

@end
