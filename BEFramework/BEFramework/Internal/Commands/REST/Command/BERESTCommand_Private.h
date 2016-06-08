//
//  BERESTCommand_Private.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BERESTCommand.h"

@interface BERESTCommand()

@property (nonatomic, copy, readwrite) NSString *sessionToken;

@property (nonatomic, copy, readwrite) NSString *httpPath;
@property (nonatomic, copy, readwrite) NSString *httpMethod;

@property (nonatomic, copy, readwrite) NSDictionary *parameters;

@property (nonatomic, copy, readwrite) NSString *cacheKey;

@property (nonatomic, copy, readwrite) NSString *operationSetUUID;

@end
