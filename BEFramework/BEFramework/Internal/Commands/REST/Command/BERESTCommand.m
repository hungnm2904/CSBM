//
//  BERESTCommand.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BERESTCommand.h"
#import "BERESTCommand_Private.h"

#import "BEAssert.h"
#import "BEFramework/BECoreManager.h"
#import "BEHTTPRequest.h"
#import "BEHash.h"
#import "BEInternalUtils.h"

static NSString *const BERESTCommandHTTPPathEncodingKey = @"httpPath";
static NSString *const BERESTCommandHTTPMethodEncodingKey = @"httpMethod";
static NSString *const BERESTCommandParametersEncodingKey = @"parameters";
static NSString *const BERESTCommandSessionTokenEncodingKey = @"sessionToken";
static NSString *const BERESTCommandLocalIdEncodingKey = @"localId";

// Increment this when you change the format of cache values.
static const int PFRESTCommandCacheKeyVersion = 1;
static const int PFRESTCommandCacheKeyParseAPIVersion = 2;

@implementation BERESTCommand

@synthesize sessionToken = _sessionToken;
@synthesize operationSetUUID = _operationSetUUID;
@synthesize localId = _localId;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

+ (instancetype)commandWithHTTPPath:(NSString *)path
                         httpMethod:(NSString *)httpMethod
                         parameters:(NSDictionary *)parameters
                       sessionToken:(NSString *)sessionToken {
  return [self commandWithHTTPPath:path
                        httpMethod:httpMethod
                        parameters:parameters
                  operationSetUUID:nil
                      sessionToken:sessionToken];
}

+ (instancetype)commandWithHTTPPath:(NSString *)path
                         httpMethod:(NSString *)httpMethod
                         parameters:(NSDictionary *)parameters
                   operationSetUUID:(NSString *)operationSetIdentifier
                       sessionToken:(NSString *)sessionToken {
  BERESTCommand *command = [[self alloc] init];
  command.httpPath = path;
  command.httpMethod = httpMethod;
  command.parameters = parameters;
  command.operationSetUUID = operationSetIdentifier;
  command.sessionToken = sessionToken;
  return command;
}

///--------------------------------------
#pragma mark - CacheKey
///--------------------------------------

//- (NSString *)cacheKey {
//  if (_cacheKey) {
//    return _cacheKey;
//  }
//  
//  NSMutableDictionary *cacheParameters = [NSMutableDictionary dictionaryWithCapacity:2];
//  if (self.parameters) {
//    cacheParameters[PFRESTCommandParametersEncodingKey] = self.parameters;
//  }
//  if (self.sessionToken) {
//    cacheParameters[PFRESTCommandSessionTokenEncodingKey] = self.sessionToken;
//  }
//  
//  NSString *parametersCacheKey = [BEInternalUtils cacheKeyForObject:cacheParameters];
//  
//  _cacheKey = [NSString stringWithFormat:@"PFRESTCommand.%i.%@.%@.%ld.%@",
//               PFRESTCommandCacheKeyVersion, self.httpMethod, BEMD5HashFromString(self.httpPath),
//               // We use MD5 instead of native hash because it collides too much.
//               (long)PFRESTCommandCacheKeyParseAPIVersion, BEMD5HashFromString(parametersCacheKey)];
//  return _cacheKey;
//}

#pragma mark Encoding/Decoding

+ (instancetype)commandFromDictionaryRepresentation:(NSDictionary *)dictionary {
  if (![self isValidDictionaryRepresentation:dictionary]) {
    return nil;
  }
  
  BERESTCommand *command = [self commandWithHTTPPath:dictionary[BERESTCommandHTTPPathEncodingKey]
                                          httpMethod:dictionary[BERESTCommandHTTPMethodEncodingKey]
                                          parameters:dictionary[BERESTCommandParametersEncodingKey]
                                        sessionToken:dictionary[BERESTCommandSessionTokenEncodingKey]];
  command.localId = dictionary[BERESTCommandLocalIdEncodingKey];
  return command;
}

- (NSDictionary *)dictionaryRepresentation {
  NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
  if (self.httpPath) {
    dictionary[BERESTCommandHTTPPathEncodingKey] = self.httpPath;
  }
  if (self.httpMethod) {
    dictionary[BERESTCommandHTTPMethodEncodingKey] = self.httpMethod;
  }
//  if (self.parameters) {
//    NSDictionary *parameters = [[BEPointerOrLocalIdObjectEncoder objectEncoder] encodeObject:self.parameters];
//    dictionary[BERESTCommandParametersEncodingKey] = parameters;
//  }
  if (self.sessionToken) {
    dictionary[BERESTCommandSessionTokenEncodingKey] = self.sessionToken;
  }
  if (self.localId) {
    dictionary[BERESTCommandLocalIdEncodingKey] = self.localId;
  }
  return [dictionary copy];
}

+ (BOOL)isValidDictionaryRepresentation:(NSDictionary *)dictionary {
  return dictionary[BERESTCommandHTTPPathEncodingKey] != nil;
}

@end
