//
//  BELogging.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#ifndef BELogging_h
#define BELogging_h

#import "BEConstants.h"
#import "BELogger.h"

static const BELoggingTag BELoggingTagCommon = 0;
static const BELoggingTag BELoggingTagCrashReporting = 100;

#define BELog(level, loggingTag, frmt, ...) \
[[BELogger sharedLogger] logMessageWithLevel:level tag:loggingTag format:(frmt), ##__VA_ARGS__]

#define BELogError(tag, frmt, ...) \
BELog(BELogLevelError, (tag), (frmt), ##__VA_ARGS__)

#define BELogWarning(tag, frmt, ...) \
BELog(BELogLevelWarning, (tag), (frmt), ##__VA_ARGS__)

#define BELogInfo(tag, frmt, ...) \
BELog(BELogLevelInfo, (tag), (frmt), ##__VA_ARGS__)

#define BELogDebug(tag, frmt, ...) \
BELog(BELogLevelDebug, (tag), (frmt), ##__VA_ARGS__)

#define BELogException(exception) \
BELogError(BELoggingTagCommon, @"Caught \"%@\" with reason \"%@\"%@", \
exception.name, exception, \
[exception callStackSymbols] ? [NSString stringWithFormat:@":\n%@.", [exception callStackSymbols]] : @"")

#endif /* BELogging_h */
