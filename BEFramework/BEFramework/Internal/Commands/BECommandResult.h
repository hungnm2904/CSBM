//
//  BECommandResult.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface BECommandResult : NSObject

@property (nonatomic, strong, readonly) id result;
@property (nullable, nonatomic, copy, readonly) NSString *resultString;
@property (nullable, nonatomic, strong, readonly) NSHTTPURLResponse *httpResponse;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

- (instancetype)initWithResult:(NSDictionary *)result
                  resultString:(nullable NSString *)resultString
                  httpResponse:(nullable NSHTTPURLResponse *)response NS_DESIGNATED_INITIALIZER;
+ (instancetype)commandResultWithResult:(NSDictionary *)result
                           resultString:(nullable NSString *)resultString
                           httpResponse:(nullable NSHTTPURLResponse *)response;

@end
