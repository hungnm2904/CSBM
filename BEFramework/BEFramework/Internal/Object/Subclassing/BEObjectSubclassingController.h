//
//  BEObjectSubclassingController.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

@class BEObject;
@protocol BESubclassing;

@interface BEObjectSubclassingController : NSObject

///--------------------------------------
#pragma mark - Init
///--------------------------------------

//TODO: (nlutsenko, richardross) Make it not terrible aka don't have singletons.
+ (instancetype)defaultController;
+ (void)clearDefaultController;

///--------------------------------------
#pragma mark - Registration
///--------------------------------------

- (Class<BESubclassing>)subclassForParseClassName:(NSString *)parseClassName;
- (void)registerSubclass:(Class<BESubclassing>)kls;
- (void)unregisterSubclass:(Class<BESubclassing>)kls;

///--------------------------------------
#pragma mark - Forwarding
///--------------------------------------

- (NSMethodSignature *)forwardingMethodSignatureForSelector:(SEL)cmd ofClass:(Class)kls;
- (BOOL)forwardObjectInvocation:(NSInvocation *)invocation withObject:(BEObject<BESubclassing> *)object;

@end
