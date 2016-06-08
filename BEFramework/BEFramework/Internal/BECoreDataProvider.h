//
//  BECoreDataProvider.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#ifndef BECoreDataProvider_h
#define BECoreDataProvider_h

@class BECurrentUserController;

@protocol BECurrentUserControllerProvider <NSObject>

@property (null_resettable, nonatomic, strong) BECurrentUserController *currentUserController;

@end

@class BEObjectController;

@protocol BEObjectControllerProvider <NSObject>

@property (null_resettable, nonatomic, strong) BEObjectController *objectController;

@end

@class BEObjectController;

@protocol BEObjectControllerProvider <NSObject>

@property (null_resettable, nonatomic, strong) BEObjectController *objectController;

@end

@class BEUserController;

@protocol BEUserControllerProvider <NSObject>

@property (null_resettable, nonatomic, strong) BEUserController *userController;

@end

@class BEUserAuthenticationController;

@protocol BEUserAuthenticationControllerProvider <NSObject>

@property (null_resettable, nonatomic, strong) BEUserAuthenticationController *userAuthenticationController;

@end

@class BECurrentUserController;

@protocol BECurrentUserControllerProvider <NSObject>

@property (null_resettable, nonatomic, strong) BECurrentUserController *currentUserController;

@end

@class BEObjectBatchController;

@protocol BEObjectBatchController <NSObject>

@property (nonatomic, strong, readonly) BEObjectBatchController *objectBatchController;

@end

@class BECurrentInstallationController;

@protocol BECurrentInstallationControllerProvider <NSObject>

@property (null_resettable, nonatomic, strong) BECurrentInstallationController *currentInstallationController;

@end

@class BEInstallationController;

@protocol BEInstallationControllerProvider <NSObject>

@property (null_resettable, nonatomic, strong) BEInstallationController *installationController;

@end


#endif /* BECoreDataProvider_h */
