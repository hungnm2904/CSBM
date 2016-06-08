//
//  BERESTCommand.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BENetworkCommand.h"

@interface BERESTCommand : NSObject <BENetworkCommand>

@property (nonatomic, copy, readonly) NSString *httpPath;
@property (nonatomic, copy, readonly) NSString *httpMethod;

@property (nullable, nonatomic, copy, readonly) NSDictionary *parameters;
@property (nullable, nonatomic, copy) NSDictionary *additionalRequestHeaders;

@property (nonatomic, copy, readonly) NSString *cacheKey;

@property (nullable, nonatomic, copy) NSString *localId;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

+ (instancetype)commandWithHTTPPath:(NSString *)path
                         httpMethod:(NSString *)httpMethod
                         parameters:(nullable NSDictionary *)parameters
                       sessionToken:(nullable NSString *)sessionToken;

+ (instancetype)commandWithHTTPPath:(NSString *)path
                         httpMethod:(NSString *)httpMethod
                         parameters:(nullable NSDictionary *)parameters
                   operationSetUUID:(nullable NSString *)operationSetIdentifier
                       sessionToken:(nullable NSString *)sessionToken;

@end
