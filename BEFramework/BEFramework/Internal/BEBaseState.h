//
//  BEBaseState.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(uint8_t, BEPropertyInfoAssociationType){
  BEPropertyInfoAssociationTypeDefault, // Assign for c-types, strong for objc-types.
  BEPropertyInfoAssociationTypeAssign,
  BEPropertyInfoAssociationTypeStrong,
  BEPropertyInfoAssociationTypeWeak,
  BEPropertyInfoAssociationTypeCopy,
  BEPropertyInfoAssociationTypeMutableCopy,
};
@interface BEPropertyAttributes : NSObject

@property (nonatomic, assign, readonly) BEPropertyInfoAssociationType associationType;

- (instancetype)initWithAssociationType:(BEPropertyInfoAssociationType)associationType NS_DESIGNATED_INITIALIZER;

+ (instancetype)attributes;
+ (instancetype)attributesWithAssociationType:(BEPropertyInfoAssociationType)associationType;

@end


@protocol BEBaseStateSubclass <NSObject>

+ (NSDictionary *)propertyAttributes;

@end

@interface BEBaseState : NSObject

- (instancetype)initWithState:(BEBaseState *)otherState;
+ (instancetype)stateWithState:(BEBaseState *)otherState;

- (NSComparisonResult)compare:(BEBaseState *)other;

- (NSDictionary *)dictionaryRepresentation;

- (id)debugQuickLookObject;

@end
