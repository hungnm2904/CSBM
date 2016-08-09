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
