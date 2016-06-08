//
//  CSBMModule.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol CSBMModule <NSObject>

- (void)csbmDidInitializeWithApplicationId:(NSString *)applicationId clientKey:(nullable NSString *)clientKey;

@end

@interface CSBMModuleCollection : NSObject <CSBMModule>

@property (nonatomic, assign, readonly) NSUInteger modulesCount;

- (void)addCSBMModule:(id<CSBMModule>)module;
- (void)removeCSBMModule:(id<CSBMModule>)module;

- (BOOL)containsModule:(id<CSBMModule>)module;

@end