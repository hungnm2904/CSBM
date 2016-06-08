//
//  BEInstallatioinIdentifierStore.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEInstallationIdentifierStore.h"

#import "BEInternalUtils.h"
#import "BEMacros.h"

static NSString *const BEInstalationIdentifierName = @"InstallationId";
@interface BEInstallationIdentifierStore() {
  
}
@property (nonatomic, copy) NSString *installationIdentifier;

@end

@implementation BEInstallationIdentifierStore

@synthesize installationIdentifier = installationIdentifier;

- (instancetype)initWithDataSource:(id<BEPersistenceControllerProvider>)dataSource {
  self = [super init];
  if (!self) return nil;
  
  _dataSource = dataSource;
  
  return self;
}
@end
