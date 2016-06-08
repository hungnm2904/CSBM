//
//  BELogger.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BELogger.h"
#import "BEApplication.h"
#import "BELogging.h"

@implementation BELogger

///--------------------------------------
#pragma mark - Class
///--------------------------------------

+ (NSString *)_descriptionForLoggingTag:(BELoggingTag)tag {
  NSString *description = nil;
  switch (tag) {
      case BELoggingTagCommon:
      break;
      case BELoggingTagCrashReporting:
      description = @"Crash Reporting";
      break;
    default:
      break;
  }
  return description;
}

+ (NSString *)_descriptionForLogLevel:(BELogLevel)logLevel {
  NSString *description = nil;
  switch (logLevel) {
      case BELogLevelNone:
      break;
      case BELogLevelDebug:
      description = @"Debug";
      break;
      case BELogLevelError:
      description = @"Error";
      break;
      case BELogLevelWarning:
      description = @"Warning";
      break;
      case BELogLevelInfo:
      description = @"Info";
      break;
  }
  return description;
}

///--------------------------------------
#pragma mark - Init
///--------------------------------------

+ (instancetype)sharedLogger {
  static BELogger *logger;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    logger = [[BELogger alloc] init];
  });
  return logger;
}

- (instancetype)init {
  self = [super init];
  if (!self) return nil;
  
  _logLevel = ([BEApplication currentApplication].appStoreEnvironment ? BELogLevelNone : BELogLevelWarning);
  
  return self;
}

///--------------------------------------
#pragma mark - Logging Messages
///--------------------------------------

- (void)logMessageWithLevel:(BELogLevel)level
                        tag:(BELoggingTag)tag
                     format:(NSString *)format, ... NS_FORMAT_FUNCTION(3, 4) {
  if (level > self.logLevel || level == BELogLevelNone || !format) {
    return;
  }
  
  va_list args;
  va_start(args, format);
  
  NSMutableString *message = [NSMutableString stringWithFormat:@"[%@]", [[self class] _descriptionForLogLevel:level]];
  
  NSString *tagDescription = [[self class] _descriptionForLoggingTag:tag];
  if (tagDescription) {
    [message appendFormat:@"[%@]", tagDescription];
  }
  
  [message appendFormat:@": %@", format];
  
  NSLogv(message, args);
  
  va_end(args);
}

@end
