//
//  BEAsyncTaskQueue.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <Bolts/BFTask.h>

@interface BEAsyncTaskQueue : NSObject


+ (instancetype)taskQueue;

- (BFTask *)enqueue:(BFContinuationBlock)block;

@end
