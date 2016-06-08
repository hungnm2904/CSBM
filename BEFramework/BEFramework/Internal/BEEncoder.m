//
//  BEEncoder.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEEncoder.h"
#import "BEAssert.h"
#import "BEObject.h"
#import "BEBase64Encoder.h"
#import "BEDateFormatter.h"

@implementation BEEncoder
+(instancetype)objectEncoder {
  static BEEncoder *encoder;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    encoder = [[BEEncoder alloc] init];
  });
  return encoder;
}

- (id)encodeObject:(id)object {
  if ([object isKindOfClass:[BEObject class]]) {
    return [self encodeParseObject:object];
  } else if ([object isKindOfClass:[NSData class]]) {
    return @{
             @"__type" : @"Bytes",
             @"base64" : [BEBase64Encoder base64StringFromData:object]
             };
  } else if([object isKindOfClass:[NSDate class]]) {
    return @{
             
             @"__type" : @"Date",
             @"iso" : [[BEDateFormatter sharedFormatter] preciseStringFromDate:object]
             };
  } else if([object isKindOfClass:[NSArray class]]) {
    NSMutableArray *newArray = [NSMutableArray arrayWithCapacity:[object count]];
    for (id elem in object) {
      [newArray addObject:[self encodeObject:elem]];
    }
    return newArray;
  } else if([object isKindOfClass:[NSDictionary class]]) {
    NSMutableDictionary *dict = [NSMutableDictionary dictionaryWithCapacity:[object count]];
    [object enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
      dict[key] = [self encodeObject:obj];
    }];
    return dict;
  }
  return object;
}

- (id)encodeParseObject:(BEObject *)object {
  return nil;
}
@end

#pragma mark - BENoObjectEncoder

@implementation BENoObjectEncoder

+(instancetype)objectEncoder {
  static BENoObjectEncoder *encoder;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    encoder = [[BENoObjectEncoder alloc] init];
  });
  return encoder;
}

- (id)encodeParseObject:(BEObject *)object {
  BEConsistencyAssertionFailure(@"BEObject are not allowed here");
  return nil;
}
@end

@implementation BEPointerOrLocalIdObjectEncoder

+ (instancetype)objectEncoder {
  static BEPointerOrLocalIdObjectEncoder *instance = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    instance = [[BEPointerOrLocalIdObjectEncoder alloc] init];
  });
  return instance;
}

- (id)encodeParseObject:(BEObject *)object {
  if (object.objectId) {
    return @{
             @"__type" : @"Pointer",
             @"objectId" : object.objectId,
             @"className" : object.parseClassName
             };
  }
  return @{
           @"__type" : @"Pointer",
           @"localId" : object.objectId,
           @"className" : object.parseClassName
           };
}

@end

///--------------------------------------
#pragma mark - BEPointerObjectEncoder
///--------------------------------------

@implementation BEPointerObjectEncoder

+ (instancetype)objectEncoder {
  static BEPointerObjectEncoder *encoder;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    encoder = [[self alloc] init];
  });
  return encoder;
}

- (id)encodeParseObject:(BEObject *)object {
  BEConsistencyAssert(object.objectId, @"Tried to save an object with a new, unsaved child.");
  return [super encodeParseObject:object];
}

@end
