/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.csbm;

import android.net.Uri;

import com.csbm.http.BEHttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Map;

/** package */ class BERESTAnalyticsCommand extends BERESTCommand {

  /**
   * The set of predefined events
   */
  // Tracks the AppOpened event
  /* package for test */ static final String EVENT_APP_OPENED = "AppOpened";

  private static final String PATH = "events/%s";

  private static final String KEY_AT = "at";
  private static final String KEY_PUSH_HASH = "push_hash";
  private static final String KEY_DIMENSIONS = "dimensions";

  public static BERESTAnalyticsCommand trackAppOpenedCommand(
          String pushHash, String sessionToken) {
    return trackEventCommand(EVENT_APP_OPENED, pushHash, null, sessionToken);
  }

  public static BERESTAnalyticsCommand trackEventCommand(
          String eventName, Map<String, String> dimensions, String sessionToken) {
    return trackEventCommand(eventName, null, dimensions, sessionToken);
  }

  /* package */ static BERESTAnalyticsCommand trackEventCommand(
          String eventName, String pushHash, Map<String, String> dimensions, String sessionToken) {
    String httpPath = String.format(PATH, Uri.encode(eventName));
    JSONObject parameters = new JSONObject();
    try {
      parameters.put(KEY_AT, NoObjectsEncoder.get().encode(new Date()));
      if (pushHash != null) {
        parameters.put(KEY_PUSH_HASH, pushHash);
      }
      if (dimensions != null) {
        parameters.put(KEY_DIMENSIONS, NoObjectsEncoder.get().encode(dimensions));
      }
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return new BERESTAnalyticsCommand(
        httpPath, BEHttpRequest.Method.POST, parameters, sessionToken);
  }

  public BERESTAnalyticsCommand(
      String httpPath,
      BEHttpRequest.Method httpMethod,
      JSONObject parameters,
      String sessionToken) {
    super(httpPath, httpMethod, parameters, sessionToken);
  }
}
