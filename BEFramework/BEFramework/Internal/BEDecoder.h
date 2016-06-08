//
//  BEDecoder.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BEDecoder : NSObject

+ (BEDecoder *)objectDecoder;

- (nullable id)decodeObject:(nullable id)object;


@end

@interface BEKnownParseObjectDecoder : BEDecoder

+ (instancetype)decoderWithFetchedObjects:(nullable NSDictionary *)fetchedObjects;

@end
NS_ASSUME_NONNULL_END