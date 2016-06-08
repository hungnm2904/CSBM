//
//  BECommandRunning.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEConstants.h"
#import "BEDataProvider.h"

@class BECancellationToken;
@class BFTask<__covariant BFGenericType>;
@class BECommandRunning;
@class BERESTCommand;
@protocol BENetworkCommand;

typedef NS_OPTIONS(NSUInteger, BECommandRunningOptions) {
  BECommandRunningOptionsRetryIfFailed = 1 << 0,
};

extern NSTimeInterval const BECommandRunningDefaultRetryDelay;

NS_ASSUME_NONNULL_BEGIN

@protocol BECommandRunning <NSObject>

@property (nonatomic, weak, readonly) id<BEInstallationIdentifierStoreProvider> dataSource;

@property (nonatomic, copy, readonly) NSString *applicationId;
@property (nonatomic, copy, readonly) NSString *clientKey;
@property (nonatomic, strong, readonly) NSURL *serverURL;

@property (nonatomic, assign) NSTimeInterval initialRetryDelay;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithDataSource:(id<BEInstallationIdentifierStoreProvider>)dataSource
                     applicationId:(NSString *)applicationId
                         clientKey:(nullable NSString *)clientKey
                         serverURL:(NSURL *)serverURL;
+ (instancetype)commandRunnerWithDataSource:(id<BEInstallationIdentifierStoreProvider>)dataSource
                              applicationId:(NSString *)applicationId
                                  clientKey:(nullable NSString *)clientKey
                                  serverURL:(NSURL *)serverURL;

///--------------------------------------
#pragma mark - Data Commands
///--------------------------------------

/**
 Run command.
 
 @param command   Command to run.
 @param options   Options to use to run command.
 
 @return `BFTask` with result set to `PFCommandResult`.
 */
- (BFTask *)runCommandAsync:(BERESTCommand *)command
                withOptions:(BECommandRunningOptions)options;

/**
 Run command.
 
 @param command           Command to run.
 @param options           Options to use to run command.
 @param cancellationToken Operation to use as a cancellation token.
 
 @return `BFTask` with result set to `PFCommandResult`.
 */
- (BFTask *)runCommandAsync:(BERESTCommand *)command
                withOptions:(BECommandRunningOptions)options
          cancellationToken:(nullable BECancellationToken *)cancellationToken;

///--------------------------------------
#pragma mark - File Commands
///--------------------------------------

- (BFTask *)runFileUploadCommandAsync:(BERESTCommand *)command
                      withContentType:(NSString *)contentType
                contentSourceFilePath:(NSString *)sourceFilePath
                              options:(BECommandRunningOptions)options
                    cancellationToken:(nullable BECancellationToken *)cancellationToken
                        progressBlock:(nullable BEProgressBlock)progressBlock;

- (BFTask *)runFileDownloadCommandAsyncWithFileURL:(NSURL *)url
                                    targetFilePath:(NSString *)filePath
                                 cancellationToken:(nullable BECancellationToken *)cancellationToken
                                     progressBlock:(nullable BEProgressBlock)progressBlock;

@end

NS_ASSUME_NONNULL_END
