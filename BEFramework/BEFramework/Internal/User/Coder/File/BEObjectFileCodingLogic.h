//
//  BEObjectFileCodingLogic.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

@class BEDecoder;
@class BEObject;

@interface BEObjectFileCodingLogic : NSObject

///--------------------------------------
#pragma mark - Init
///--------------------------------------

+ (instancetype)codingLogic;

///--------------------------------------
#pragma mark - Logic
///--------------------------------------

- (void)updateObject:(BEObject *)object fromDictionary:(NSDictionary *)dictionary usingDecoder:(BEDecoder *)decoder;

@end
