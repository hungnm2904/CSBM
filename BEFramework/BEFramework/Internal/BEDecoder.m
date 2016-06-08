//
//  BEDecoder.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEDecoder.h"

#import "BEBase64Encoder.h"
#import "BEDateFormatter.h"
#import "BEMacros.h"

@implementation BEDecoder

+ (BEDecoder *)objectDecoder {
  static BEDecoder *decoder;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    decoder = [[BEDecoder alloc] init];
  });
  return decoder;
}

#pragma mark Decode

- (id)decodeDictionary:(NSDictionary *)dictonary {
  NSString *op = dictonary[@"__op"];
  if (op) {
    
  }
  NSString *type = dictonary[@"__type"];
  if (type) {
    if([type isEqualToString:@"Date"]) {
      return [[BEDateFormatter sharedFormatter] dateFromString:dictonary[@"iso"]];
    } else if([type isEqualToString:@"Bytes"]) {
      return [BEBase64Encoder dataFromBase64String:dictonary[@"base64"]];
    } else if([type isEqualToString:@"Object"]) {
//      NSString *className = dictonary[@"className"];
//      
//      NSMutableDictionary *data = [dictonary mutableCopy];
//      [data removeObjectForKey:@"__type"];
//      [data removeObjectForKey:@"className"];
//      NSDictionary *result = [self decodeDictionary:data];
//      
//      return [BEObject _obj]
    } else {
      return dictonary;
    }
  }
  NSMutableDictionary *newDictionary = [NSMutableDictionary dictionaryWithCapacity:dictonary.count];
  [dictonary enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
    newDictionary[key] = [self decodeObject:obj];
  }];
  return newDictionary;
}

// decode for pointer classname.

- (id)decodeArray:(NSArray *)array {
  NSMutableArray *newArray = [NSMutableArray arrayWithCapacity:array.count];
  for(id value in array) {
    [newArray addObject:[self decodeObject:value]];
  }
  return newArray;
}
@end


///--------------------------------------
#pragma mark - BEKnownParseObjectDecoder
///--------------------------------------

@interface BEKnownParseObjectDecoder ()

@property (nonatomic, copy) NSDictionary *fetchedObjects;

@end

@implementation BEKnownParseObjectDecoder

+ (instancetype)decoderWithFetchedObjects:(NSDictionary *)fetchedObjects {
  BEKnownParseObjectDecoder *decoder = [[self alloc] init];
  decoder.fetchedObjects = fetchedObjects;
  return decoder;
}

//- (id)_decodePointerForClassName:(NSString *)className objectId:(NSString *)objectId {
//  if (_fetchedObjects != nil && _fetchedObjects[objectId]) {
//    return _fetchedObjects[objectId];
//  }
//  return [super _decodePointerForClassName:className objectId:objectId];
//}

@end
