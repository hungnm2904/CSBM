//
//  BEAssert.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//
#import "BEMacros.h"

#ifndef BEAssert_h
#define BEAssert_h

/**
 Raises an `NSInvalidArgumentException` if the `condition` does not pass.
 Use `description` to supply the way to fix the exception.
 */
#define BEParameterAssert(condition, description, ...) \
do {\
if (!(condition)) { \
[NSException raise:NSInvalidArgumentException \
format:description, ##__VA_ARGS__]; \
} \
} while(0)

/**
 Raises an `NSInvalidArgumentException`. Use `description` to supply the way to fix the exception.
 */
#define BEParameterAssertionFailure(description, ...) \
do {\
[NSException raise:NSInvalidArgumentException \
format:description, ##__VA_ARGS__]; \
} while(0)

/**
 Raises an `NSRangeException` if the `condition` does not pass.
 Use `description` to supply the way to fix the exception.
 */
#define BERangeAssert(condition, description, ...) \
do {\
if (!(condition)) { \
[NSException raise:NSRangeException \
format:description, ##__VA_ARGS__]; \
} \
} while(0)

/**
 Raises an `NSInternalInconsistencyException` if the `condition` does not pass.
 Use `description` to supply the way to fix the exception.
 */
#define BEConsistencyAssert(condition, description, ...) \
do { \
if (!(condition)) { \
[NSException raise:NSInternalInconsistencyException \
format:description, ##__VA_ARGS__]; \
} \
} while(0)

/**
 Raises an `NSInternalInconsistencyException`. Use `description` to supply the way to fix the exception.
 */
#define BEConsistencyAssertionFailure(description, ...) \
do {\
[NSException raise:NSInternalInconsistencyException \
format:description, ##__VA_ARGS__]; \
} while(0)

/**
 Always raises `NSInternalInconsistencyException` with details
 about the method used and class that received the message
 */
#define BENotDesignatedInitializer() \
do { \
BEConsistencyAssertionFailure(@"%@ is not the designated initializer for instances of %@.", \
NSStringFromSelector(_cmd), \
NSStringFromClass([self class])); \
return nil; \
} while (0)

/**
 Raises `NSInternalInconsistencyException` if current thread is not main thread.
 */
#define BEAssertMainThread() \
do { \
BEConsistencyAssert([NSThread isMainThread], @"This method must be called on the main thread."); \
} while (0)

/**
 Raises `NSInternalInconsistencyException` if current thread is not the required one.
 */
#define BEAssertIsOnThread(thread) \
do { \
BEConsistencyAssert([NSThread currentThread] == thread, \
@"This method must be called only on thread: %@.", thread); \
} while (0)

/**
 Raises `NSInternalInconsistencyException` if the current queue
 is not the same as the queue provided.
 Make sure you mark the queue first via `BEMarkDispatchQueue`
 */
#define BEAssertIsOnDispatchQueue(queue) \
do { \
void *mark = BEOSObjectPointer(queue); \
BEConsistencyAssert(dispatch_get_specific(mark) == mark, \
                    @"%s must be executed on %s", \
                    __PRETTY_FUNCTION__, dispatch_queue_get_label(queue)); \
} while (0)

#endif /* BEAssert_h */
