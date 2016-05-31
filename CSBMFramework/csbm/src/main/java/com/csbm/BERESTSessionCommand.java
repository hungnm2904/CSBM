/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.csbm;


import com.csbm.http.BEHttpRequest;

import org.json.JSONObject;

/** package */ class BERESTSessionCommand extends BERESTCommand {

  public static BERESTSessionCommand getCurrentSessionCommand(String sessionToken) {
    return new BERESTSessionCommand(
        "sessions/me", BEHttpRequest.Method.GET, null, sessionToken);
  }

  public static BERESTSessionCommand revoke(String sessionToken) {
    return new BERESTSessionCommand(
        "logout", BEHttpRequest.Method.POST, new JSONObject(), sessionToken);
  }

  public static BERESTSessionCommand upgradeToRevocableSessionCommand(String sessionToken) {
    return new BERESTSessionCommand(
        "upgradeToRevocableSession", BEHttpRequest.Method.POST, new JSONObject(), sessionToken);
  }

  private BERESTSessionCommand(
      String httpPath,
      BEHttpRequest.Method httpMethod,
      JSONObject jsonParameters,
      String sessionToken) {
    super(httpPath, httpMethod, jsonParameters, sessionToken);
  }
}
