//
//  BECommandCache.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BECommandCache.h"
#include <mach/mach_time.h>
#include <sys/xattr.h>

#import <Bolts/BFTask.h>
#import <Bolts/BFTaskCompletionSource.h>

#import "BFTask+Private.h"
#import "BEAssert.h"
#import "BECommandResult.h"
#import "BECoreManager.h"
#import "BEErrorUtilities.h"
#import "BEEventuallyQueue_Private.h"
#import "BELogging.h"
#import "BEMacros.h"
#import "BEObject.h"
#import "BEObjectPrivate.h"
#import "BERESTCommand.h"
#import "CSBM_Private.h"

static NSString *const _BECommandCacheDiskCacheDirectoryName = @"Command Cache";

static const NSString *BECommandCachePrefixString = @"Command";
static unsigned long long const BECommandCacheDefaultDiskCacheSize = 10 * 1024 * 1024; // 10 MB

@interface BECommandCache () <BEEventuallyQueueSubclass> {
  unsigned int _fileCounter;
}

@property (nonatomic, assign, readwrite, setter=_setDiskCacheSize:) unsigned long long diskCacheSize;

@end

@implementation BECommandCache

@dynamic dataSource;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

+ (instancetype)newDefaultCommandCacheWithCommonDataSource:(id<BECommandRunnerProvider>)dataSource
                                            coreDataSource:(id<BEObjectLocalIdStoreProvider>)coreDataSource
                                           cacheFolderPath:(NSString *)cacheFolderPath {
  NSString *diskCachePath = [cacheFolderPath stringByAppendingPathComponent:_BECommandCacheDiskCacheDirectoryName];
  diskCachePath = diskCachePath.stringByStandardizingPath;
  BECommandCache *cache = [[BECommandCache alloc] initWithDataSource:dataSource
                                                      coreDataSource:coreDataSource
                                                    maxAttemptsCount:BEEventuallyQueueDefaultMaxAttemptsCount
                                                       retryInterval:BEEventuallyQueueDefaultTimeoutRetryInterval
                                                       diskCachePath:diskCachePath
                                                       diskCacheSize:BECommandCacheDefaultDiskCacheSize];
  [cache start];
  return cache;
}

- (instancetype)initWithDataSource:(id<BECommandRunnerProvider>)dataSource
                    coreDataSource:(id<BEObjectLocalIdStoreProvider>)coreDataSource
                  maxAttemptsCount:(NSUInteger)attemptsCount
                     retryInterval:(NSTimeInterval)retryInterval
                     diskCachePath:(NSString *)diskCachePath
                     diskCacheSize:(unsigned long long)diskCacheSize {
  self = [super initWithDataSource:dataSource maxAttemptsCount:attemptsCount retryInterval:retryInterval];
  if (!self) return nil;
  
  _coreDataSource = coreDataSource;
  _diskCachePath = diskCachePath;
  _diskCacheSize = diskCacheSize;
  _fileCounter = 0;
  
  //[self _createDiskCachePathIfNeeded];
  
  return self;
}

///--------------------------------------
#pragma mark - Controlling Queue
///--------------------------------------

- (void)removeAllCommands {
  [self pause];
  
  [super removeAllCommands];
  
  NSArray *commandIdentifiers = [self _pendingCommandIdentifiers];
  NSMutableArray *tasks = [NSMutableArray arrayWithCapacity:commandIdentifiers.count];
  
//  for (NSString *identifier in commandIdentifiers) {
//    BFTask *task = [self _removeFileForCommandWithIdentifier:identifier];
//    [tasks addObject:task];
//  }
  
  [[BFTask taskForCompletionOfAllTasks:tasks] waitUntilFinished];
  
  [self resume];
}

///--------------------------------------
#pragma mark - BEEventuallyQueue
///--------------------------------------

- (void)_simulateReboot {
  [super _simulateReboot];
  //[self _createDiskCachePathIfNeeded];
}

///--------------------------------------
#pragma mark - BEEventuallyQueueSubclass
///--------------------------------------

- (NSString *)_newIdentifierForCommand:(id<BENetworkCommand>)command {
  // Start with current time - so we can sort identifiers and get the oldest one first.
  return [NSString stringWithFormat:@"%@-%016qx-%08x-%@",
          BECommandCachePrefixString,
          (unsigned long long)[NSDate timeIntervalSinceReferenceDate],
          _fileCounter++,
          [NSUUID UUID].UUIDString];
}

- (NSArray *)_pendingCommandIdentifiers {
  NSArray *result = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:self.diskCachePath error:nil];
  // Only accept files that starts with "Command" since sometimes the directory is filled with garbage
  // e.g.: https://phab.parse.com/file/info/PHID-FILE-qgbwk7sm7kcyaks6n4j7/
  result = [result filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"SELF BEGINSWITH %@", BECommandCachePrefixString]];
  
  return [result sortedArrayUsingSelector:@selector(compare:)];
}

//- (id<BENetworkCommand>)_commandWithIdentifier:(NSString *)identifier error:(NSError **)error {
//  [[BEMultiProcessFileLockController sharedController] beginLockedContentAccessForFileAtPath:self.diskCachePath];
//  
//  NSError *innerError = nil;
//  NSData *jsonData = [NSData dataWithContentsOfFile:[self _filePathForCommandWithIdentifier:identifier]
//                                            options:NSDataReadingUncached
//                                              error:&innerError];
//  
//  [[BEMultiProcessFileLockController sharedController] endLockedContentAccessForFileAtPath:self.diskCachePath];
//  
//  if (innerError || !jsonData) {
//    NSString *message = [NSString stringWithFormat:@"Failed to read command from cache. %@",
//                         innerError ? innerError.localizedDescription : @""];
//    innerError = [BEErrorUtilities errorWithCode:kBEErrorInternalServer
//                                         message:message];
//    if (error) {
//      *error = innerError;
//    }
//    return nil;
//  }
//  
//  id jsonObject = [NSJSONSerialization JSONObjectWithData:jsonData
//                                                  options:0
//                                                    error:&innerError];
//  if (innerError) {
//    NSString *message = [NSString stringWithFormat:@"Failed to deserialiaze command from cache. %@",
//                         innerError.localizedDescription];
//    innerError = [BEErrorUtilities errorWithCode:kBEErrorInternalServer
//                                         message:message];
//  } else {
//    if ([BERESTCommand isValidDictionaryRepresentation:jsonObject]) {
//      return [BERESTCommand commandFromDictionaryRepresentation:jsonObject];
//    }
//    innerError = [BEErrorUtilities errorWithCode:kBEErrorInternalServer
//                                         message:@"Failed to construct eventually command from cache."
//                                       shouldLog:NO];
//  }
//  if (innerError && error) {
//    *error = innerError;
//  }
//  
//  return nil;
//}

//- (BFTask *)_enqueueCommandInBackground:(id<BENetworkCommand>)command
//                                 object:(BEObject *)object
//                             identifier:(NSString *)identifier {
//  return [self _saveCommandToCacheInBackground:command object:object identifier:identifier];
//}

//- (BFTask *)_didFinishRunningCommand:(id<BENetworkCommand>)command
//                      withIdentifier:(NSString *)identifier
//                          resultTask:(BFTask *)resultTask {
//  // Get the new objectId and mark the new localId so it can be resolved.
//  if (command.localId) {
//    NSDictionary *dictionaryResult = nil;
//    if ([resultTask.result isKindOfClass:[NSDictionary class]]) {
//      dictionaryResult = resultTask.result;
//    } else if ([resultTask.result isKindOfClass:[BECommandResult class]]) {
//      BECommandResult *commandResult = resultTask.result;
//      dictionaryResult = commandResult.result;
//    }
//    
//    if (dictionaryResult != nil) {
//      NSString *objectId = dictionaryResult[@"objectId"];
//      if (objectId) {
//        [self.coreDataSource.objectLocalIdStore setObjectId:objectId forLocalId:command.localId];
//      }
//    }
//  }
//  
//  [[self _removeFileForCommandWithIdentifier:identifier] waitUntilFinished];
//  return [super _didFinishRunningCommand:command withIdentifier:identifier resultTask:resultTask];
//}

- (BFTask *)_waitForOperationSet:(BEOperationSet *)operationSet eventuallyPin:(BEEventuallyPin *)eventuallyPin {
  // Do nothing. This is only relevant in BEPinningEventuallyQueue. Looks super hacky you said? Yes it is!
  return [BFTask taskWithResult:nil];
}

///--------------------------------------
#pragma mark - Disk Cache
///--------------------------------------

//- (BFTask *)_cleanupDiskCacheWithRequiredFreeSize:(NSUInteger)requiredSize {
//  return [BFTask taskFromExecutor:[BFExecutor defaultExecutor] withBlock:^id{
//    NSUInteger size = requiredSize;
//    
//    NSMutableDictionary<NSString *, NSNumber *> *commandSizes = [NSMutableDictionary dictionary];
//    
//    [[BEMultiProcessFileLockController sharedController] beginLockedContentAccessForFileAtPath:self.diskCachePath];
//    
//    NSDictionary *directoryAttributes = [[NSFileManager defaultManager] attributesOfItemAtPath:self.diskCachePath error:nil];
//    if ([directoryAttributes[NSFileSize] unsignedLongLongValue] > self.diskCacheSize) {
//      NSDirectoryEnumerator<NSURL *> *enumerator = [[NSFileManager defaultManager] enumeratorAtURL:[NSURL fileURLWithPath:self.diskCachePath]
//                                                                        includingPropertiesForKeys:@[ NSURLFileSizeKey ]
//                                                                                           options:NSDirectoryEnumerationSkipsSubdirectoryDescendants
//                                                                                      errorHandler:nil];
//      NSURL *fileURL = nil;
//      while ((fileURL = [enumerator nextObject])) {
//        NSNumber *fileSize = nil;
//        if (![fileURL getResourceValue:&fileSize forKey:NSURLFileSizeKey error:nil]) {
//          continue;
//        }
//        if (fileSize) {
//          commandSizes[fileURL.path.lastPathComponent] = fileSize;
//          size += fileSize.unsignedIntegerValue;
//        }
//      }
//    }
//    
//    [[BEMultiProcessFileLockController sharedController] endLockedContentAccessForFileAtPath:self.diskCachePath];
//    
//    if (size > self.diskCacheSize) {
//      // Get identifiers and sort them to remove oldest commands first
//      NSArray<NSString *> *identifiers = [commandSizes.allKeys sortedArrayUsingSelector:@selector(compare:)];
//      for (NSString *identifier in identifiers) @autoreleasepool {
//        [self _removeFileForCommandWithIdentifier:identifier];
//        size -= [commandSizes[identifier] unsignedIntegerValue];
//        
//        if (size <= self.diskCacheSize) {
//          break;
//        }
//        [commandSizes removeObjectForKey:identifier];
//      }
//    }
//    
//    return nil;
//  }];
//}

- (void)_setDiskCacheSize:(unsigned long long)diskCacheSize {
  _diskCacheSize = diskCacheSize;
}

///--------------------------------------
#pragma mark - Files
///--------------------------------------

//- (BFTask *)_saveCommandToCacheInBackground:(id<BENetworkCommand>)command
//                                     object:(BEObject *)object
//                                 identifier:(NSString *)identifier {
//  if (object != nil && object.objectId == nil) {
//    command.localId = [object getOrCreateLocalId];
//  }
//  
//  @weakify(self);
//  return [BFTask taskFromExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:^id{
//    @strongify(self);
//    
//    NSError *error = nil;
//    NSData *data = [NSJSONSerialization dataWithJSONObject:[command dictionaryRepresentation]
//                                                   options:0
//                                                     error:&error];
//    NSUInteger commandSize = data.length;
//    if (commandSize > self.diskCacheSize) {
//      error = [BEErrorUtilities errorWithCode:kBEErrorInternalServer
//                                      message:@"Failed to run command, because it's too big."];
//    } else if (commandSize == 0) {
//      error = [BEErrorUtilities errorWithCode:kBEErrorInternalServer
//                                      message:@"Failed to run command, because it's empty."];
//    }
//    
//    if (error) {
//      return [BFTask taskWithError:error];
//    }
//    
//    [[BEMultiProcessFileLockController sharedController] beginLockedContentAccessForFileAtPath:self.diskCachePath];
//    return [[[self _cleanupDiskCacheWithRequiredFreeSize:commandSize] continueWithBlock:^id(BFTask *task) {
//      NSString *filePath = [self _filePathForCommandWithIdentifier:identifier];
//      return [BEFileManager writeDataAsync:data toFile:filePath];
//    }] continueWithBlock:^id(BFTask *task) {
//      [[BEMultiProcessFileLockController sharedController] endLockedContentAccessForFileAtPath:self.diskCachePath];
//      return task;
//    }];
//  }];
//}
//
//- (BFTask *)_removeFileForCommandWithIdentifier:(NSString *)identifier {
//  NSString *filePath = [self _filePathForCommandWithIdentifier:identifier];
//  return [[BFTask taskFromExecutor:[BFExecutor defaultPriorityBackgroundExecutor] withBlock:^id{
//    [[BEMultiProcessFileLockController sharedController] beginLockedContentAccessForFileAtPath:self.diskCachePath];
//    return [BEFileManager removeItemAtPathAsync:filePath withFileLock:NO];
//  }] continueWithBlock:^id(BFTask *task) {
//    [[BEMultiProcessFileLockController sharedController] endLockedContentAccessForFileAtPath:self.diskCachePath];
//    return task; // Roll-forward the previous task.
//  }];
//}
//
//- (NSString *)_filePathForCommandWithIdentifier:(NSString *)identifier {
//  return [self.diskCachePath stringByAppendingPathComponent:identifier];
//}
//
//- (void)_createDiskCachePathIfNeeded {
//  [[BEFileManager createDirectoryIfNeededAsyncAtPath:_diskCachePath] waitForResult:nil withMainThreadWarning:NO];
//}

@end
