package com.csbm;


import com.csbm.http.BEHttpRequest;

import java.util.HashMap;
import java.util.Map;

/** package */ class BERESTConfigCommand extends BERESTCommand {

  public BERESTConfigCommand(
      String httpPath,
      BEHttpRequest.Method httpMethod,
      Map<String, ?> parameters,
      String sessionToken) {
    super(httpPath, httpMethod, parameters, sessionToken);
  }

  public static BERESTConfigCommand fetchConfigCommand(String sessionToken) {
    return new BERESTConfigCommand("config", BEHttpRequest.Method.GET, null, sessionToken);
  }

  public static BERESTConfigCommand updateConfigCommand(
          final Map<String, ?> configParameters, String sessionToken) {
    Map<String, Map<String, ?>> commandParameters = null;
    if (configParameters != null) {
      commandParameters = new HashMap<>();
      commandParameters.put("params", configParameters);
    }
    return new BERESTConfigCommand(
        "config", BEHttpRequest.Method.PUT, commandParameters, sessionToken);
  }
}
