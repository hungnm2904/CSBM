//
//  BEEncoder.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "BEConstants.h"

#import "BEMacros.h"

@class BFTask<__covariant BFGenericType>;
@class BEObject;

@interface BEEncoder : NSObject
+ (instancetype)objectEncoder;

- (id)encodeObject:(id)object;
- (id)encodeParseObject:(BEObject *)object;

@end

/**
 *  Encoding rejects BEObject.
 */
@interface BEPointerOrLocalIdObjectEncoder : BEEncoder

@end


@interface BEPointerObjectEncoder : BEPointerOrLocalIdObjectEncoder

@end

@interface BENoObjectEncoder : BEEncoder

@end
