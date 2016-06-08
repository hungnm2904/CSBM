//
//  BEClientConfiguration.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <BEConstants.h>

@protocol CSBMMutableClientConfiguration <NSObject>

#pragma mark - Connecting to Parse

/**
 *  The CSBM application Id to configure the framework with.
 */
@property (nullable, nonatomic, copy) NSString *applicationId;

/**
 *  The CSBM client Key to configure the framework with.
 */
@property (nullable, nonatomic, copy) NSString *clientKey;

/**
 *  The server url that is configure the framework with.
 */
@property (nonatomic, copy) NSString *server;

#pragma mark - Other properties

/**
 *  The maximum number of retry
 */
@property (nonatomic, assign) NSUInteger networkRetryAttempts;

@end

@interface BEClientConfiguration : NSObject <NSCopying>

#pragma mark - Connection to Parse

@property (nullable, nonatomic, copy, readonly) NSString *applicationId;

@property (nullable, nonatomic, copy, readonly) NSString *clientKey;

@property (nonatomic, copy, readonly) NSString *server;

@property (nonatomic, assign, readonly) NSUInteger networkRetryAttempts;

+ (instancetype)configurationWithBlock:(void (^)(id<CSBMMutableClientConfiguration> configuration))configuration;

+ (instancetype)new NS_UNAVAILABLE;
+ (instancetype)init NS_UNAVAILABLE;
@end
