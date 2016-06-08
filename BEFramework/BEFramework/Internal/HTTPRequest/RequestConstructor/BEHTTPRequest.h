//
//  BEHTTPRequest.h
//  BEFramework
//
//  Created by Loc Nguyen on 5/25/16.
//  Copyright © 2016 Loc Nguyen. All rights reserved.
//

#ifndef BEHTTPRequest_h
#define BEHTTPRequest_h

#import <Foundation/Foundation.h>

static NSString *const BEHTTPRequestMethodGET = @"GET";
static NSString *const BEHTTPRequestMethodHEAD = @"HEAD";
static NSString *const BEHTTPRequestMethodDELETE = @"DELETE";
static NSString *const BEHTTPRequestMethodPOST = @"POST";
static NSString *const BEHTTPRequestMethodPUT = @"PUT";

static NSString *const BEHTTPRequestHeaderNameContentType = @"Content-Type";
static NSString *const BEHTTPRequestHeaderNameContentLength = @"Content-Length";

#endif /* BEHTTPRequest_h */
