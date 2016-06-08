//
//  BEPropertyInfo.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "BEBaseState.h"

@interface BEPropertyInfo : NSObject

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

- (instancetype)initWithClass:(Class)kls
                         name:(NSString *)propertyName;

- (instancetype)initWithClass:(Class)kls
                         name:(NSString *)propertyName
              associationType:(BEPropertyInfoAssociationType)associationType NS_DESIGNATED_INITIALIZER;

+ (instancetype)propertyInfoWithClass:(Class)kls
                                 name:(NSString *)propertyName;

+ (instancetype)propertyInfoWithClass:(Class)kls
                                 name:(NSString *)propertyName
                      associationType:(BEPropertyInfoAssociationType)associationType;

@property (nonatomic, copy, readonly) NSString *name;
@property (nonatomic, readonly) BEPropertyInfoAssociationType associationType;

/**
 Returns the value of this property,
 properly wrapped from the target object.
 When possible, just invokes the property.
 When not, uses -valueForKey:.
 */
- (nullable id)getWrappedValueFrom:(id)object;
- (void)setWrappedValue:(nullable id)value forObject:(id)object;

// Moves the value from one object to the other, based on the association type given.
- (void)takeValueFrom:(id)one toObject:(id)two;

@end
