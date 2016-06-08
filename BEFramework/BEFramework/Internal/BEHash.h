//
//  BEHash.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

extern NSUInteger BEIntegerPairHash(NSUInteger a, NSUInteger b);

extern NSUInteger BEDoublePairHash(double a, double b);

extern NSUInteger BEDoubleHash(double d);

extern NSUInteger BELongHash(unsigned long long l);

extern NSString *BEMD5HashFromData(NSData *data);
extern NSString *BEMD5HashFromString(NSString *string);
