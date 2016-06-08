//
//  BEObjectPrivate.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEObject.h"
#import <Bolts/BFTask.h>
#import "BEDecoder.h"
#import "BEEncoder.h"
#import "BEMulticastDelegate.h"
#import "BEObjectControlling.h"
#import "BESubclassing.h"

@class BFTask<__covariant BFGenericType>;
@class BECurrentUserController;
@class BEFieldOperation;
@class BEJSONCacheItem;
@class BEMultiCommand;
@class BEObjectEstimatedData;
@class BEObjectFileCodingLogic;
@class BEObjectState;
@class BEObjectSubclassingController;
@class BEOperationSet;
@class BERESTCommand;
@class BETaskQueue;

@protocol BEObjectPrivateSubclass <NSObject>

@required

///--------------------------------------
#pragma mark - State
///--------------------------------------

+ (BEObjectState *)_newObjectStateWithParseClassName:(NSString *)className
                                            objectId:(NSString *)objectId
                                          isComplete:(BOOL)complete;

@optional

///--------------------------------------
#pragma mark - Before Save
///--------------------------------------

/**
 Called before an object is going to be saved. Called in a context of object lock.
 Subclasses can override this method to do any custom updates before an object gets saved.
 */
- (void)_objectWillSave;

@end

@interface BEObject () {
@protected
  BOOL dirty;
  
  // An array of NSDictionary of NSString -> PFFieldOperation.
  // Each dictionary has a subset of the object's keys as keys, and the
  // changes to the value for that key as its value.
  // There is always at least one dictionary of pending operations.
  // Every time a save is started, a new dictionary is added to the end.
  // Whenever a save completes, the new data is put into fetchedData, and
  // a dictionary is removed from the start.
  NSMutableArray *operationSetQueue;
}

/**
 @return Current object state.
 */
@property (nonatomic, copy) BEObjectState *_state;
@property (nonatomic, copy) NSMutableSet *_availableKeys;

- (instancetype)initWithObjectState:(BEObjectState *)state;
+ (instancetype)objectWithClassName:(NSString *)className
                           objectId:(NSString *)objectid
                       completeData:(BOOL)completeData;
+ (instancetype)objectWithoutDataWithClassName:(NSString *)className localId:(NSString *)localId;

- (BETaskQueue *)taskQueue;

- (BEObjectEstimatedData *)_estimatedData;

#if PF_TARGET_OS_OSX
// Not available publicly, but available for testing

- (instancetype)refresh;
- (instancetype)refresh:(NSError **)error;
- (void)refreshInBackgroundWithBlock:(PFObjectResultBlock)block;

#endif

///--------------------------------------
#pragma mark - Validation
///--------------------------------------

- (BFTask<BEVoid> *)_validateFetchAsync NS_REQUIRES_SUPER;
- (BFTask<BEVoid> *)_validateDeleteAsync NS_REQUIRES_SUPER;

/**
 Validate the save eventually operation with the current state.
 The result of this task is ignored. The error/cancellation/exception will prevent `saveEventually`.
 
 @return Task that encapsulates the validation.
 */
- (BFTask<BEVoid> *)_validateSaveEventuallyAsync NS_REQUIRES_SUPER;

///--------------------------------------
#pragma mark - Pin
///--------------------------------------
- (BFTask *)_pinInBackgroundWithName:(NSString *)name includeChildren:(BOOL)includeChildren;
+ (BFTask *)_pinAllInBackground:(NSArray *)objects withName:(NSString *)name includeChildren:(BOOL)includeChildren;

+ (id<BEObjectControlling>)objectController;
+ (BEObjectFileCodingLogic *)objectFileCodingLogic;
+ (BECurrentUserController *)currentUserController;

///--------------------------------------
#pragma mark - Subclassing
///--------------------------------------

+ (BEObjectSubclassingController *)subclassingController;
@end

@interface BEObject (Private)


/**
 Returns the object that should be used to synchronize all internal data access.
 */
- (NSObject *)lock;

/**
 Blocks until all outstanding operations have completed.
 */
- (void)waitUntilFinished;

- (NSDictionary *)_collectFetchedObjects;

///--------------------------------------
#pragma mark - Static methods for Subclassing
///--------------------------------------

/**
 Unregisters a class registered using registerSubclass:
 If we ever expose thsi method publicly, we must change the underlying implementation
 to have stack behavior. Currently unregistering a custom class for a built-in will
 leave the built-in unregistered as well.
 @param subclass the subclass
 */
+ (void)unregisterSubclass:(Class<BESubclassing>)subclass;

///--------------------------------------
#pragma mark - Children helpers
///--------------------------------------
- (BFTask *)_saveChildrenInBackgroundWithCurrentUser:(BEUser *)currentUser sessionToken:(NSString *)sessionToken;

///--------------------------------------
#pragma mark - Dirtiness helpers
///--------------------------------------
- (BOOL)isDirty:(BOOL)considerChildren;
- (void)_setDirty:(BOOL)dirty;

- (void)performOperation:(BEFieldOperation *)operation forKey:(NSString *)key;
- (void)setHasBeenFetched:(BOOL)fetched;
- (void)_setDeleted:(BOOL)deleted;

- (BOOL)isDataAvailableForKey:(NSString *)key;

- (BOOL)_hasChanges;
- (BOOL)_hasOutstandingOperations;
- (BEOperationSet *)unsavedChanges;

///--------------------------------------
#pragma mark - Validations
///--------------------------------------
- (void)_checkSaveParametersWithCurrentUser:(BEUser *)currentUser;
/**
 Checks if Parse class name could be used to initialize a given instance of PFObject or it's subclass.
 */
+ (void)_assertValidInstanceClassName:(NSString *)className;

///--------------------------------------
#pragma mark - Serialization helpers
///--------------------------------------
- (NSString *)getOrCreateLocalId;
- (void)resolveLocalId;

+ (id)_objectFromDictionary:(NSDictionary *)dictionary
           defaultClassName:(NSString *)defaultClassName
               completeData:(BOOL)completeData;

+ (id)_objectFromDictionary:(NSDictionary *)dictionary
           defaultClassName:(NSString *)defaultClassName
               selectedKeys:(NSArray *)selectedKeys;

+ (id)_objectFromDictionary:(NSDictionary *)dictionary
           defaultClassName:(NSString *)defaultClassName
               completeData:(BOOL)completeData
                    decoder:(BEDecoder *)decoder;
+ (BFTask *)_migrateObjectInBackgroundFromFile:(NSString *)fileName toPin:(NSString *)pinName;
+ (BFTask *)_migrateObjectInBackgroundFromFile:(NSString *)fileName
                                         toPin:(NSString *)pinName
                           usingMigrationBlock:(BFContinuationBlock)block;

- (NSMutableDictionary *)_convertToDictionaryForSaving:(BEOperationSet *)changes
                                     withObjectEncoder:(BEEncoder *)encoder;
///--------------------------------------
#pragma mark - REST operations
///--------------------------------------
- (NSDictionary *)RESTDictionaryWithObjectEncoder:(BEEncoder *)objectEncoder
                                operationSetUUIDs:(NSArray **)operationSetUUIDs;
- (NSDictionary *)RESTDictionaryWithObjectEncoder:(BEEncoder *)objectEncoder
                                operationSetUUIDs:(NSArray **)operationSetUUIDs
                                            state:(BEObjectState *)state
                                operationSetQueue:(NSArray *)queue
                          deletingEventuallyCount:(NSUInteger)deletingEventuallyCount;

- (void)mergeFromRESTDictionary:(NSDictionary *)object
                    withDecoder:(BEDecoder *)decoder;

///--------------------------------------
#pragma mark - Data helpers
///--------------------------------------
- (void)rebuildEstimatedData;

///--------------------------------------
#pragma mark - Command handlers
///--------------------------------------
- (PFObject *)mergeFromObject:(PFObject *)other;

- (void)_mergeAfterSaveWithResult:(NSDictionary *)result decoder:(BEDecoder *)decoder;
- (void)_mergeAfterFetchWithResult:(NSDictionary *)result decoder:(BEDecoder *)decoder completeData:(BOOL)completeData;
- (void)_mergeFromServerWithResult:(NSDictionary *)result decoder:(BEDecoder *)decoder completeData:(BOOL)completeData;

- (BFTask *)handleSaveResultAsync:(NSDictionary *)result;

///--------------------------------------
#pragma mark - Asynchronous operations
///--------------------------------------
- (void)startSave;
- (BFTask *)_enqueueSaveEventuallyWithChildren:(BOOL)saveChildren;
- (BFTask *)saveAsync:(BFTask *)toAwait;
- (BFTask *)fetchAsync:(BFTask *)toAwait;
- (BFTask *)deleteAsync:(BFTask *)toAwait;

///--------------------------------------
#pragma mark - Command constructors
///--------------------------------------
- (BERESTCommand *)_constructSaveCommandForChanges:(BEOperationSet *)changes
                                      sessionToken:(NSString *)sessionToken
                                     objectEncoder:(BEEncoder *)encoder;
- (BERESTCommand *)_currentDeleteCommandWithSessionToken:(NSString *)sessionToken;

///--------------------------------------
#pragma mark - Misc helpers
///--------------------------------------
- (NSString *)displayClassName;
- (NSString *)displayObjectId;

- (void)registerSaveListener:(void (^)(id result, NSError *error))callback;
- (void)unregisterSaveListener:(void (^)(id result, NSError *error))callback;
//- (BEACL *)ACLWithoutCopying;

///--------------------------------------
#pragma mark - Get and set
///--------------------------------------

- (void)_setObject:(id)object
            forKey:(NSString *)key
   onlyIfDifferent:(BOOL)onlyIfDifferent;

///--------------------------------------
#pragma mark - Subclass Helpers
///--------------------------------------

/**
 This method is called by -[PFObject init]; changes made to the object during this
 method will not mark the object as dirty. PFObject uses this method to to apply the
 default ACL; subclasses which override this method shold be sure to call the super
 implementation if they want to honor the default ACL.
 */
- (void)setDefaultValues;

/**
 This method allows subclasses to determine whether a default ACL should be applied
 to new instances.
 */
- (BOOL)needsDefaultACL;
@end

@interface BEObject () {
  BEMulticastDelegate *saveDelegate;
}

@property (nonatomic, strong) BEMulticastDelegate *saveDelegate;

@end

