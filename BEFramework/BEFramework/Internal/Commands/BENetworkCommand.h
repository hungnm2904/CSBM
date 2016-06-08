//
//  BENetworkCommand.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol BENetworkCommand <NSObject>

///--------------------------------------
#pragma mark - Properties
///--------------------------------------

@property (nonatomic, copy, readonly) NSString *sessionToken;
@property (nonatomic, copy, readonly) NSString *operationSetUUID;

// If this command creates an object that is referenced by some other command,
// then this localId will be updated with the new objectId that is returned.
@property (nonatomic, copy) NSString *localId;

///--------------------------------------
#pragma mark - Encoding/Decoding
///--------------------------------------

+ (instancetype)commandFromDictionaryRepresentation:(NSDictionary *)dictionary;
- (NSDictionary *)dictionaryRepresentation;

+ (BOOL)isValidDictionaryRepresentation:(NSDictionary *)dictionary;

///--------------------------------------
#pragma mark - Local Identifiers
///--------------------------------------

/**
 Replaces all local ids in this command with the correct objectId for that object.
 This should be called before sending the command over the network, so that there
 are no local ids sent to the Parse Cloud. If any local id refers to an object that
 has not yet been saved, and thus has no objectId, then this method raises an
 exception.
 */
- (void)resolveLocalIds;

@end
