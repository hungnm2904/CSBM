//
//  BEMutableObjectState.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEMutableObjectState.h"

#import "BEDateFormatter.h"
#import "BEObjectState_Private.h"

@implementation BEMutableObjectState

@dynamic csbmClassName;
@dynamic objectId;
@dynamic createdAt;
@dynamic updatedAt;
@dynamic serverData;
@dynamic complete;
@dynamic deleted;

///--------------------------------------
#pragma mark - PFMutableObjectState
///--------------------------------------

#pragma mark Accessors

- (void)setServerDataObject:(id)object forKey:(NSString *)key {
  [super setServerDataObject:object forKey:key];
}

- (void)removeServerDataObjectForKey:(NSString *)key {
  [super removeServerDataObjectForKey:key];
}

- (void)removeServerDataObjectsForKeys:(NSArray *)keys {
  [super removeServerDataObjectsForKeys:keys];
}

- (void)setCreatedAtFromString:(NSString *)string {
  [super setCreatedAtFromString:string];
}

- (void)setUpdatedAtFromString:(NSString *)string {
  [super setUpdatedAtFromString:string];
}

#pragma mark Apply

- (void)applyState:(BEObjectState *)state {
  [super applyState:state];
}

- (void)applyOperationSet:(BEOperationSet *)operationSet {
  [super applyOperationSet:operationSet];
}

@end
