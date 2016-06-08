//
//  BEEventuallyQueue.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEConstants.h"
#import "BEMacros.h"
#import "BENetworkCommand.h"

@class BFTask<__covariant BFGenericType>;
@class BEEventuallyPin;
@class BEEventuallyQueueTestHelper;
@class BEObject;
@protocol BECommandRunnerProvider;

extern NSUInteger const BEEventuallyQueueDefaultMaxAttemptsCount;
extern NSTimeInterval const BEEventuallyQueueDefaultTimeoutRetryInterval;

@interface BEEventuallyQueue : NSObject

@property (nonatomic, weak, readonly) id<BECommandRunnerProvider> dataSource;

@property (nonatomic, assign, readonly) NSUInteger maxAttemptsCount;
@property (nonatomic, assign, readonly) NSTimeInterval retryInterval;

@property (nonatomic, assign, readonly) NSUInteger commandCount;

/**
 Controls whether the queue should monitor network reachability and pause itself when there is no connection.
 Default: `YES`.
 */
@property (atomic, assign, readonly) BOOL monitorsReachability BE_WATCH_UNAVAILABLE;
@property (nonatomic, assign, readonly, getter=isConnected) BOOL connected;

// Gets notifications of various events happening in the command cache, so that tests can be synchronized.
@property (nonatomic, strong, readonly) BEEventuallyQueueTestHelper *testHelper;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

- (instancetype)initWithDataSource:(id<BECommandRunnerProvider>)dataSource
                  maxAttemptsCount:(NSUInteger)attemptsCount
                     retryInterval:(NSTimeInterval)retryInterval NS_DESIGNATED_INITIALIZER;

///--------------------------------------
#pragma mark - Running Commands
///--------------------------------------

- (BFTask *)enqueueCommandInBackground:(id<BENetworkCommand>)command;
- (BFTask *)enqueueCommandInBackground:(id<BENetworkCommand>)command withObject:(BEObject *)object;

///--------------------------------------
#pragma mark - Controlling Queue
///--------------------------------------

- (void)start NS_REQUIRES_SUPER;
- (void)resume NS_REQUIRES_SUPER;
- (void)pause NS_REQUIRES_SUPER;

- (void)removeAllCommands NS_REQUIRES_SUPER;

@end

typedef enum {
  BEEventuallyQueueEventCommandEnqueued, // A command was placed into the queue.
  BEEventuallyQueueEventCommandNotEnqueued, // A command could not be placed into the queue.
  
  BEEventuallyQueueEventCommandSucceded, // A command has successfully running on the server.
  BEEventuallyQueueEventCommandFailed, // A command has failed on the server.
  
  BEEventuallyQueueEventObjectUpdated, // An object's data was updated after a command completed.
  BEEventuallyQueueEventObjectRemoved, // An object was removed because it was deleted before creation.
  
  BEEventuallyQueueEventCount // The total number of items in this enum.
} BEEventuallyQueueTestHelperEvent;

@interface BEEventuallyQueueTestHelper : NSObject {
  dispatch_semaphore_t events[BEEventuallyQueueEventCount];
}

- (void)clear;
- (void)notify:(BEEventuallyQueueTestHelperEvent)event;
- (BOOL)waitFor:(BEEventuallyQueueTestHelperEvent)event;

@end