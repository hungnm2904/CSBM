//
//  BERESTQueryCommand.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BERESTCommand.h"

@class BEQueryState;


@interface BERESTQueryCommand : BERESTCommand

///--------------------------------------
#pragma mark - Find
///--------------------------------------

+ (instancetype)findCommandForQueryState:(BEQueryState *)queryState withSessionToken:(nullable NSString *)sessionToken;

+ (instancetype)findCommandForClassWithName:(NSString *)className
                                      order:(nullable NSString *)order
                                 conditions:(nullable NSDictionary *)conditions
                               selectedKeys:(nullable NSSet *)selectedKeys
                               includedKeys:(nullable NSSet *)includedKeys
                                      limit:(NSInteger)limit
                                       skip:(NSInteger)skip
                               extraOptions:(nullable NSDictionary *)extraOptions
                             tracingEnabled:(BOOL)trace
                               sessionToken:(nullable NSString *)sessionToken;

///--------------------------------------
#pragma mark - Count
///--------------------------------------

+ (instancetype)countCommandFromFindCommand:(BERESTQueryCommand *)findCommand;

///--------------------------------------
#pragma mark - Parameters
///--------------------------------------

+ (NSDictionary *)findCommandParametersForQueryState:(BEQueryState *)queryState;
+ (NSDictionary *)findCommandParametersWithOrder:(nullable NSString *)order
                                      conditions:(nullable NSDictionary *)conditions
                                    selectedKeys:(nullable NSSet *)selectedKeys
                                    includedKeys:(nullable NSSet *)includedKeys
                                           limit:(NSInteger)limit
                                            skip:(NSInteger)skip
                                    extraOptions:(nullable NSDictionary *)extraOptions
                                  tracingEnabled:(BOOL)trace;

@end