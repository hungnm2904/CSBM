//
//  BEPropeprtyInfo_Private.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <objc/runtime.h>
#import "BEPropertyInfo.h"

@interface BEPropertyInfo()

@property (atomic, assign, readonly) Class sourceClass;
@property (atomic, assign, readonly, getter=isObject) BOOL object;

@property (atomic, copy, readonly) NSString *typeEncoding;
@property (atomic, assign, readonly) Ivar ivar;

@property (atomic, assign, readonly) SEL getterSelector;
@property (atomic, assign, readonly) SEL setterSelector;

@end
