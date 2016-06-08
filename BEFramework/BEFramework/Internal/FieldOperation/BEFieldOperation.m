//
//  BEFieldOperation.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEFieldOperation.h"

#import "BEAssert.h"
#import "BEDecoder.h"
#import "BEObject.h"
#import "BEInternalUtils.h"

@implementation BEFieldOperation

- (id)encodeWithObjectEncoder:(BEEncoder *)objectEncoder {
  
  return nil;
}

- (BEFieldOperation *)mergeWithPrevious:(BEFieldOperation *)previous {
  return nil;
}

- (id)applyToValue:(id)oldValue forKey:(NSString *)key {
  return nil;
}

@end

@implementation BESetOperation
- (instancetype)init {
  BENotDesignatedInitializer();
}
- (instancetype)initWithValue:(id)value {
  self = [super init];
  if (!self) return nil;
  
  BEParameterAssert(value, @"Cannot set a nil");
  _value = value;
  return self;
}
+ (instancetype)setWithValue:(id)newvalue {
  return [[self alloc] initWithValue:newvalue];
}

- (NSString *)description {
  return [NSString stringWithFormat:@"set to %@", self.value];
}

- (id)encodeWithObjectEncoder:(BEEncoder *)objectEncoder {
  return [objectEncoder encodeObject:self.value];
}

- (BEFieldOperation *)mergeWithPrevious:(BEFieldOperation *)previous {
  return self;
}

- (id)applyToValue:(id)oldValue forKey:(NSString *)key {
  return self.value;
}

@end

@implementation BEDeleteOperation

+ (instancetype)operation {
  return [[self alloc] init];
}

- (NSString *)description {
  return @"delete";
}

- (id)encodeWithObjectEncoder:(BEEncoder *)objectEncoder {
  return @{ @"__op" : @"Delete" };
}

- (BEFieldOperation *)mergeWithPrevious:(BEFieldOperation *)previous {
  return self;
}

- (id)applyToValue:(id)oldValue forKey:(NSString *)key {
  return nil;
}

@end

@implementation BEIncrementOperation

- (instancetype)init {
  BENotDesignatedInitializer();
}

- (instancetype)initWithAmount:(NSNumber *)amount {
  self = [super init];
  if (!self) return nil;
  _amount = amount;
  
  return self;
}

+ (instancetype)incrementWithAmount:(NSNumber *)amount {
  return [[self alloc] initWithAmount:amount];
}

- (NSString *)description {
  return [NSString stringWithFormat:@"increment by %@", self.amount];
}

- (id)encodeWithObjectEncoder:(BEEncoder *)objectEncoder {
  return @{@"__op": @"Increment", @"amount" : self.amount};
}

- (BEFieldOperation *)mergeWithPrevious:(BEFieldOperation *)previous {
  if(!previous) {
    return self;
  } else if([previous isKindOfClass:[BEDeleteOperation class]]) {
    return [BESetOperation setWithValue:self.amount];
  } else if ([previous isKindOfClass:[BESetOperation class]]) {
    id oldValue = ((BESetOperation *)previous).value;
    return [BESetOperation setWithValue:[BEInternalUtils addNumber:self.amount withNumber:oldValue]];
  }else if([previous isKindOfClass:[BEIncrementOperation class]]) {
    NSNumber *newAmmount = [BEInternalUtils addNumber:self.amount withNumber:((BEIncrementOperation*)previous).amount];
    return [BEIncrementOperation incrementWithAmount:newAmmount];
  }
  return nil;
}
- (id)applyToValue:(id)oldValue forKey:(NSString *)key {
  if (!oldValue) {
    return self.amount;
  }
  return [BEInternalUtils addNumber:self.amount withNumber:oldValue];
}
@end

// chua implement interface addoperation