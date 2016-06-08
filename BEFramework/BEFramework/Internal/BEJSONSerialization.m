//
//  BEJSONSerialization.m
//  BEFramework
//
//  Created by Loc Nguyen on 5/26/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#import "BEJSONSerialization.h"

#import "BEAssert.h"
#import "BELogging.h"

@implementation BEJSONSerialization
+ (NSData *)dataFromJSONObject:(id)object {
  NSError *error = nil;
  NSData *data = [NSJSONSerialization dataWithJSONObject:object options:0 error:&error];
  BEParameterAssert(data && !error, @"BEObject values must be serializable to JSON");
  
  return data;
}

+ (NSString *)stringFromJSONObject:(id)object {
  NSData *data = [self dataFromJSONObject:object];
  return [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
}

+ (id)JSONObjectFromData:(NSData *)data {
  NSError *error = nil;
  id object = [NSJSONSerialization JSONObjectWithData:data
                                              options:0
                                                error:&error];
  if (!object || error != nil) {
    BELogError(BELoggingTagCommon, @"JSON deserialization failed with error: %@", [error description]);
  }
  
  return object;
}

+ (id)JSONObjectFromString:(NSString *)string {
  return [self JSONObjectFromData:[string dataUsingEncoding:NSUTF8StringEncoding]];
}

+ (id)JSONObjectFromFileAtPath:(NSString *)filePath {
  NSInputStream *stream = [NSInputStream inputStreamWithFileAtPath:filePath];
  if (!stream) {
    return nil;
  }
  
  [stream open];
  
  NSError *streamError = stream.streamError;
  // Check if stream failed to open, because there is no such file.
  if (streamError && [streamError.domain isEqualToString:NSPOSIXErrorDomain] && streamError.code == ENOENT) {
    [stream close]; // Still close the stream.
    return nil;
  }
  
  NSError *error = nil;
  id object = [NSJSONSerialization JSONObjectWithStream:stream options:0 error:&error];
  if (!object || error) {
    BELogError(BELoggingTagCommon, @"JSON deserialization failed with error: %@", error.description);
  }
  
  [stream close];
  
  return object;
}
@end
