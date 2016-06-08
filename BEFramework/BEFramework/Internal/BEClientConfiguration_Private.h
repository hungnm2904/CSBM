//
//  BEClientConfiguration_Private.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <BEClientConfiguration.h>

extern NSString *const _CSBMDefaultServerURLString;

@interface BEClientConfiguration()

@property (nullable, nonatomic, copy, readwrite) NSString *applicationId;
@property (nullable, nonatomic, copy, readwrite) NSString *clientKey;

@property (nonatomic, copy, readwrite) NSString *server;

@property (nonatomic, assign, readwrite, getter=isLocalDatastoreEnabled) BOOL localDatastoreEnabled;

//@property (nullable, nonatomic, copy, readwrite) NSString *applicationGroupIdentifier;
//@property (nullable, nonatomic, copy, readwrite) NSString *containingApplicationBundleIdentifier;

@property (nonatomic, assign, readwrite) NSUInteger networkRetryAttempts;

+ (instancetype)emptyConfiguration;
- (instancetype)initEmpty NS_DESIGNATED_INITIALIZER;

- (void)_resetDataSharingIdentifiers;

@end

@interface BEClientConfiguration (Private) <CSBMMutableClientConfiguration>

@end