//
//  BELogger.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BEConstants.h"

typedef uint8_t BELoggingTag;

@interface BELogger : NSObject
@property (atomic, assign) BELogLevel logLevel;

///--------------------------------------
#pragma mark - Shared Logger
///--------------------------------------

/**
 A shared instance of `PFLogger` that should be used for all logging.
 
 @return An shared singleton instance of `PFLogger`.
 */
+ (instancetype)sharedLogger; //TODO: (nlutsenko) Convert to use an instance everywhere instead of a shared singleton.

///--------------------------------------
#pragma mark - Logging Messages
///--------------------------------------

/**
 Logs a message at a specific level for a tag.
 If current logging level doesn't include this level - this method does nothing.
 
 @param level  Logging Level
 @param tag    Logging Tag
 @param format Format to use for the log message.
 */
- (void)logMessageWithLevel:(BELogLevel)level
                        tag:(BELoggingTag)tag
                     format:(NSString *)format, ... NS_FORMAT_FUNCTION(3, 4);
@end
