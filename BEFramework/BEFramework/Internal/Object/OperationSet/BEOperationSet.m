//
//  BEOperationSet.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEOperationSet.h"
#import "BEDecoder.h"
#import "BEEncoder.h"
#import "BEFieldOperation.h"

static NSString *const BEOperationSetKeyUUID = @"__uuid";
static NSString *const BEOperationSetKeyIsSaveEvenually = @"__isSaveEventually";
static NSString *const BEOperationSetKeyUpdateAt = @"__updateAt";
static NSString *const BEOperationSetKeyACL = @"ACL";

@interface BEOperationSet()

@property (nonatomic, strong) NSMutableDictionary *dictionary;

@end
@implementation BEOperationSet
#pragma mark - Init
- (instancetype)init {
  return [self initWithUUID: [NSUUID UUID].UUIDString];
}
-(instancetype)initWithUUID:(NSString *)uuid {
  self = [super init];
  if (!self) return nil;
  
  _dictionary = [NSMutableDictionary dictionary];
  _uuid = [uuid copy];
  
  _updatedAt = [NSDate date];
  
  return self;
}

#pragma mark - Encoding

- (NSDictionary *)RESTDictionaryUsingObjectEncoder:(BEEncoder *)objectEncoder operationSetUUID:(NSArray **)operatioSETUUIDs {
  NSMutableDictionary *operationSetResult = [[NSMutableDictionary alloc] init];
  [self.dictionary enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
    operationSetResult[key] = [obj encodeWithObjectEncoder:objectEncoder];
  }];
  operationSetResult[BEOperationSetKeyUUID] = self.uuid;
  operationSetResult[BEOperationSetKeyUpdateAt] = [objectEncoder encodeObject:self.updatedAt];
  
  if (self.saveEventually) {
    operationSetResult[BEOperationSetKeyIsSaveEvenually] = @YES;
  }
  *operatioSETUUIDs = @[ self.uuid ];
  return operationSetResult;
}

+ (BEOperationSet *)operationSetFromRESTDictionary:(NSDictionary *)data usingDecoder:(BEDecoder *)decoder {
  NSMutableDictionary *mutableData = [data mutableCopy];
  NSString *inputUUID = mutableData[BEOperationSetKeyUUID];
  [mutableData removeObjectForKey:BEOperationSetKeyUUID];
  BEOperationSet *operationSet = nil;
  if (inputUUID == nil) {
    operationSet = [[BEOperationSet alloc] init];
  } else {
    operationSet = [[BEOperationSet alloc] initWithUUID:inputUUID];
  }
  
  NSNumber *saveEventuallyFlag = mutableData[BEOperationSetKeyIsSaveEvenually];
  if (saveEventuallyFlag) {
    operationSet.saveEventually = saveEventuallyFlag.boolValue;
    [mutableData removeObjectForKey:BEOperationSetKeyIsSaveEvenually];
  }
  
  NSDate *updatedAt = mutableData[BEOperationSetKeyUpdateAt];
  [mutableData removeObjectForKey:BEOperationSetKeyUpdateAt];
  
  [mutableData enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
    id value = [decoder decodeObject:obj];
//    BEFieldOperation *fieldOperation = nil;
//    if ([key isEqualToString:BEOperationSetKeyACL]) {
//      // TODO (hallucinogen): where to use the decoder?
//      value = [PFACL ACLWithDictionary:obj];
//    }
//    if ([value isKindOfClass:[BEFieldOperation class]]) {
//      fieldOperation = value;
//    } else {
//      fieldOperation = [BESetOperation setWithValue:value];
//    }
//    operationSet[key] = fieldOperation;
  }];
  operationSet.updatedAt = updatedAt ? [decoder decodeObject:updatedAt] : nil;
  
  return operationSet;
}

#pragma mark - Accerssors
- (id)objectForKey:(id)aKey {
  return self.dictionary[aKey];
}

- (id)objectForKeyedSubscript:(id)aKey {
  return [self objectForKey:aKey];
}

- (NSUInteger)count {
  return self.dictionary.count;
}

- (NSEnumerator *)keyEnumerator {
  return [self.dictionary keyEnumerator];
}

- (void)enumerateKeysAndObjectsUsingBlock:(void (^)(NSString *key, BEFieldOperation *operation, BOOL *stop))block {
  [self.dictionary enumerateKeysAndObjectsUsingBlock:block];
}

- (void)setObject:(id)anObject forKey:(id<NSCopying>)aKey {
  self.dictionary[aKey] = anObject;
  self.updatedAt = [NSDate date];
}

- (void)setObject:(id)anObject forKeyedSubscript:(id<NSCopying>)key {
  [self setObject:anObject forKey:key];
}

- (void)removeObjectForKey:(id)key {
  [self.dictionary removeObjectForKey:key];
  self.updatedAt = [NSDate date];
}

- (void)removeAllObjects {
  [self.dictionary removeAllObjects];
  self.updatedAt = [NSDate date];
}

///--------------------------------------
#pragma mark - NSFastEnumeration
///--------------------------------------

- (NSUInteger)countByEnumeratingWithState:(NSFastEnumerationState *)state
                                  objects:(id __unsafe_unretained [])buffer
                                    count:(NSUInteger)len {
  return [self.dictionary countByEnumeratingWithState:state objects:buffer count:len];
}

///--------------------------------------
#pragma mark - NSCopying
///--------------------------------------

- (instancetype)copyWithZone:(NSZone *)zone {
  BEOperationSet *operationSet = [[[self class] allocWithZone:zone] initWithUUID:self.uuid];
  operationSet.dictionary = [self.dictionary mutableCopy];
  operationSet.updatedAt = [self.updatedAt copy];
  operationSet.saveEventually = self.saveEventually;
  return operationSet;
}
@end
