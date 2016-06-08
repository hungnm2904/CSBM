//
//  BEMacros.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//
#import <Foundation/NSObjCRuntime.h>
#import <os/object.h>

#ifndef BEMacros_h
#define BEMacros_h

/**
 This macro allows to create NSSet via subscript.
 */
#define BE_SET(...)  [NSSet setWithObjects:__VA_ARGS__, nil]

/**
 This macro is a handy thing for converting libSystem objects to (void *) pointers.
 If you are targeting OSX 10.8+ and iOS 6.0+ - this is no longer required.
 */
#if OS_OBJECT_USE_OBJC
#define BEOSObjectPointer(object) \
(__bridge void *)(object)
#else
#define BEOSObjectPointer(object) \
(void *)(object)
#endif

/**
 Mark a queue in order to be able to check BEAssertIsOnMarkedQueue.
 */
#define BEMarkDispatchQueue(queue) \
dispatch_queue_set_specific((queue), \
BEOSObjectPointer(queue), \
BEOSObjectPointer(queue), \
NULL)

///--------------------------------------
#pragma mark - Memory Management
///
/// The following macros are influenced and include portions of libextobjc.
///--------------------------------------

/**
 Creates a __weak version of the variable provided,
 which can later be safely used or converted into strong variable via @strongify.
 */
#define weakify(var) \
try {} @catch (...) {} \
__weak __typeof__(var) var ## _weak = var;

/**
 Creates a strong shadow reference of the variable provided.
 Variable must have previously been passed to @weakify.
 */
#define strongify(var) \
try {} @catch (...) {} \
__strong __typeof__(var) var = var ## _weak;

///--------------------------------------
#pragma mark - KVC
///--------------------------------------

/**
 This macro ensures that object.key exists at compile time.
 It can accept a chained key path.
 */
#define keypath(TYPE, PATH) \
(((void)(NO && ((void)((TYPE *)(nil)).PATH, NO)), # PATH))

///--------------------------------------
#pragma mark - Runtime
///--------------------------------------

/**
 Using objc_msgSend directly is bad, very bad. Doing so without casting could result in stack-smashing on architectures
 (*cough* x86 *cough*) that use strange methods of returning values of different types.
 
 The objc_msgSend_safe macro ensures that we properly cast the function call to use the right conventions when passing
 parameters and getting return values. This also fixes some issues with ARC and objc_msgSend directly, though strange
 things can happen when receiving values from NS_RETURNS_RETAINED methods.
 */
#define objc_msgSend(...)  _Pragma("GCC error \"Use objc_msgSend_safe() instead!\"")
#define objc_msgSend_safe(returnType, argTypes...) ((returnType (*)(id, SEL, ##argTypes))(objc_msgSend))

/**
 This exists because if we throw an exception from dispatch_sync, it doesn't 'bubble up' to the calling thread.
 This simply wraps dispatch_sync and properly throws the exception back to the calling thread, not the thread that
 the exception was originally raised on.
 
 @param queue The queue to execute on
 @param block The block to execute
 
 @see dispatch_sync
 */
#define BE_sync_with_throw(queue, block)      \
do {                                      \
__block NSException *caught = nil;    \
dispatch_sync(queue, ^{              \
@try { block(); }                 \
@catch (NSException *ex) {        \
caught = ex;                  \
}                                 \
});                                   \
if (caught) @throw caught;            \
} while (0)

/**
 To prevent retain cycles by OCMock, this macro allows us to capture a weak reference to return from a stubbed method.
 */
#define andReturnWeak(variable) _andDo(                                              \
({                                                                               \
__weak typeof(variable) variable ## _weak = (variable);                      \
^(NSInvocation *invocation) {                                                \
__autoreleasing typeof(variable) variable ## _block = variable ## _weak; \
[invocation setReturnValue:&(variable ## _block)];                       \
};                                                                           \
})                                                                               \
)

/**
 This exists to use along with bolts generic tasks. Instead of returning a BFTask with no generic type, or a generic
 type of 'NSNull' when there is no usable result from a task, we use the type 'BEVoid', which will always have a value
 of  'nil'.
 
 This allows us to more easily descern between methods that have not yet updated the return type of their tasks, as well
 as provide a more enforced API contract to the caller (as sending any message to BEVoid will result in a compile time
 error).
 */
@class _BEVoid_Nonexistant;
typedef _BEVoid_Nonexistant *BEVoid;

#endif /* BEMacros_h */
