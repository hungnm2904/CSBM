//
//  BERESTObjectCommand.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BERESTCommand.h"

@class BEObjectState;

@interface BERESTObjectCommand : BERESTCommand

+ (instancetype)fetchObjectCommandForObjectState:(BEObjectState *)state
                                withSessionToken:(nullable NSString *)sessionToken;

+ (instancetype)createObjectCommandForObjectState:(BEObjectState *)state
                                          changes:(nullable NSDictionary *)changes
                                 operationSetUUID:(nullable NSString *)operationSetIdentifier
                                     sessionToken:(nullable NSString *)sessionToken;

+ (instancetype)updateObjectCommandForObjectState:(BEObjectState *)state
                                          changes:(nullable NSDictionary *)changes
                                 operationSetUUID:(nullable NSString *)operationSetIdentifier
                                     sessionToken:(nullable NSString *)sessionToken;

+ (instancetype)deleteObjectCommandForObjectState:(BEObjectState *)state
                                 withSessionToken:(nullable NSString *)sessionToken;

@end
