//
//  BEObjectController_Private.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEObjectController.h"

@class BERESTCommand;

@interface BEObjectController ()

///--------------------------------------
#pragma mark - Fetch
///--------------------------------------

- (BFTask *)_runFetchCommand:(BERESTCommand *)command forObject:(BEObject *)object;

@end

