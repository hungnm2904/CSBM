package com.csbm;


import com.csbm.http.BEHttpRequest;

import java.util.Map;

/** package */ class BERESTCloudCommand extends BERESTCommand {

  private BERESTCloudCommand(
      String httpPath,
      BEHttpRequest.Method httpMethod,
      Map<String, ?> parameters,
      String sessionToken) {
    super(httpPath, httpMethod, parameters, sessionToken);
  }

  public static BERESTCloudCommand callFunctionCommand(String functionName,
                                                          Map<String, ?> parameters, String sessionToken) {
    final String httpPath = String.format("functions/%s", functionName);
    return new BERESTCloudCommand(
        httpPath, BEHttpRequest.Method.POST, parameters, sessionToken);
  }
}
