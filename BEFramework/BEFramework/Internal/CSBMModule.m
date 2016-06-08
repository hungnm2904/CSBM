//
//  CSBMModule.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "CSBMModule.h"

typedef void (^CSBMModuleEnumerationBlock)(id<CSBMModule> module, BOOL *stop, BOOL *remove);

@interface CSBMModuleCollection ()

@property (atomic, strong) dispatch_queue_t collectionQueue;
@property (atomic, strong) NSPointerArray *modules;

@end

@implementation CSBMModuleCollection

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init {
  self = [super init];
  if (!self) return self;
  
  _collectionQueue = dispatch_queue_create("com.parse.CSBMModuleCollection", DISPATCH_QUEUE_SERIAL);
  _modules = [NSPointerArray weakObjectsPointerArray];
  
  return self;
}

///--------------------------------------
#pragma mark - Collection
///--------------------------------------

- (void)addCSBMModule:(id<CSBMModule>)module {
  [self performCollectionAccessBlock:^{
    [self.modules addPointer:(__bridge void *)module];
  }];
}

- (void)removeCSBMModule:(id<CSBMModule>)module {
  [self enumerateModulesWithBlock:^(id<CSBMModule> enumeratedModule, BOOL *stop, BOOL *remove) {
    *remove = (module == enumeratedModule);
  }];
}

- (BOOL)containsModule:(id<CSBMModule>)module {
  __block BOOL retValue = NO;
  [self enumerateModulesWithBlock:^(id<CSBMModule> enumeratedModule, BOOL *stop, BOOL *remove) {
    if (module == enumeratedModule) {
      retValue = YES;
      *stop = YES;
    }
  }];
  return retValue;
}

- (NSUInteger)modulesCount {
  return self.modules.count;
}

///--------------------------------------
#pragma mark - CSBMModule
///--------------------------------------

- (void)parseDidInitializeWithApplicationId:(NSString *)applicationId clientKey:(nullable NSString *)clientKey {
  dispatch_async(dispatch_get_main_queue(), ^{
    [self enumerateModulesWithBlock:^(id<CSBMModule> module, BOOL *stop, BOOL *remove) {
      [module csbmDidInitializeWithApplicationId:applicationId clientKey:clientKey];
    }];
  });
}

///--------------------------------------
#pragma mark - Private
///--------------------------------------

- (void)performCollectionAccessBlock:(dispatch_block_t)block {
  dispatch_sync(self.collectionQueue, block);
}

/**
 Enumerates all existing modules in this collection.
 
 NOTE: This **will modify the contents of the collection** if any of the modules were deallocated since last loop.
 
 @param block the block to enumerate with.
 */
- (void)enumerateModulesWithBlock:(CSBMModuleEnumerationBlock)block {
  [self performCollectionAccessBlock:^{
    NSMutableIndexSet *toRemove = [[NSMutableIndexSet alloc] init];
    
    NSUInteger index = 0;
    BOOL stop = NO;
    
    for (__strong id<CSBMModule> module in self.modules) {
      BOOL remove = module == nil;
      if (!remove) {
        block(module, &stop, &remove);
      }
      
      if (remove) {
        [toRemove addIndex:index];
      }
      
      if (stop) break;
      index++;
    }
    
    // NSPointerArray doesn't have a -removeObjectsAtIndexes:... WHY!?!?
    for (index = toRemove.firstIndex; index != NSNotFound; index = [toRemove indexGreaterThanIndex:index]) {
      [self.modules removePointerAtIndex:index];
    }
  }];
}

@end
