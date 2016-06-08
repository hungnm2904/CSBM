//
//  BEQueryPrivate.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEQuery.h"
#import "BEQueryState.h"

@class BFTask<__covariant BFGenericType>;
@class BEObject;

@interface BEQuery (Private)

@property (nonatomic, strong, readonly) BEQueryState *state;

- (instancetype)whereRelatedToObject:(BEObject *)parent fromKey:(NSString *)key;
- (void)redirectClassNameForKey:(NSString *)key;

@end
