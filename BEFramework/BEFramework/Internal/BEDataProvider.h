//
//  BEDataProvider.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#ifndef BEDataProvider_h
#define BEDataProvider_h

@protocol BECommandRunning;

@protocol BECommandRunnerProvider <NSObject>

@property (nonatomic, strong, readonly) id<BECommandRunning> commandRunner;

@end

@class BEPersistenceController;

@protocol BEPersistenceControllerProvider <NSObject>

@property (nonatomic, strong, readonly) BEPersistenceController *persistenceController;

@end

@class BEInstallationIdentifierStore;

@protocol BEInstallationIdentifierStoreProvider <NSObject>

@property (nonatomic, strong, readonly) BEInstallationIdentifierStore *installationIdentifierStore;

@end

@class BEKeyValueCache;

@protocol BEKeyValueCacheProvider <NSObject>

@property (nonatomic, strong, readonly) BEKeyValueCache *keyValueCache;

@end

#endif /* BEDataProvider_h */
