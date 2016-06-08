//
//  BEClientConfiguration.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEClientConfiguration.h"

#import "BEAssert.h"
#import "BECommandRunningConstants.h"
#import "BEHash.h"

NSString *const _CSBMDefaultServerURLString = @"https://api.parse.com/1";

@implementation BEClientConfiguration

#pragma mark - Init

+ (instancetype)emptyConfiguration {
  return [[super alloc] initEmpty];
}

- (instancetype)initEmpty {
  self = [super init];
  if (!self) return nil;
  
  _networkRetryAttempts = BECommandRunningDefaultMaxAttemptsCount;
  _server = [_CSBMDefaultServerURLString copy];
  
  return self;
}

- (instancetype)initWithBlock:(void (^)(id<CSBMMutableClientConfiguration>))configurationBlock {
  self = [super init];
  if (!self) return nil;
  
  configurationBlock(self);
  
  BEConsistencyAssert(self.applicationId.length, @"`applicationId` should not be nil");
  
  return self;
}

+ (instancetype)configurationWithBlock:(void (^)(id<CSBMMutableClientConfiguration>))configuration {
  return [[self alloc] initWithBlock:configuration];
}

#pragma mark - Properties

- (void)setApplicationId:(NSString * _Nullable)applicationId {
  BEParameterAssert(applicationId.length, @"`applicationId` should not be nil");
  _applicationId = [applicationId copy];
}

- (void)setClientKey:(NSString * _Nullable)clientKey {
  _clientKey = [clientKey copy];
}

- (void)setServer:(NSString *)server {
  BEParameterAssert(server.length, @"Server should not be nil");
  BEParameterAssert([NSURL URLWithString:server], @"Server should be a valid url");
  _server = [server copy];
}

#pragma mark - NSObject

- (NSInteger)hash {
  return BEIntegerPairHash(self.applicationId.hash, self.clientKey.hash);
}
//
//- (BOOL)isEqual:(id)object {
//  
//}
@end
