//
//  BEObjectSubclassInfo.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

@class BEPropertyInfo;

@interface BEObjectSubclassInfo : NSObject

@property (atomic, strong) Class subclass;

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

- (instancetype)initWithSubclass:(Class)kls NS_DESIGNATED_INITIALIZER;
+ (instancetype)subclassInfoWithSubclass:(Class)kls;

- (BEPropertyInfo *)propertyInfoForSelector:(SEL)cmd isSetter:(BOOL *)isSetter;
- (NSMethodSignature *)forwardingMethodSignatureForSelector:(SEL)cmd;

@end
