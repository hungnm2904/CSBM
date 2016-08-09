package com.csbm;


import com.csbm.http.BEHttpRequest;
import com.csbm.http.BEHttpResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import bolts.Task;

/** package */ class BERESTUserCommand extends BERESTCommand {

  private static final String HEADER_REVOCABLE_SESSION = "X-CSBM-Revocable-Session";
  private static final String HEADER_TRUE = "1";

  public static BERESTUserCommand getCurrentUserCommand(String sessionToken) {
    return new BERESTUserCommand("users/me", BEHttpRequest.Method.GET, null, sessionToken);
  }

  //region Authentication

  public static BERESTUserCommand signUpUserCommand(JSONObject parameters, String sessionToken,
                                                       boolean revocableSession) {
    return new BERESTUserCommand(
        "classes/_User", BEHttpRequest.Method.POST, parameters, sessionToken, revocableSession);
  }

  public static BERESTUserCommand logInUserCommand(String username, String password,
                                                      boolean revocableSession) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("username", username);
    parameters.put("password", password);
    return new BERESTUserCommand(
        "login", BEHttpRequest.Method.GET, parameters, null, revocableSession);
  }

  public static BERESTUserCommand serviceLogInUserCommand(
          String authType, Map<String, String> authData, boolean revocableSession) {

    // Mimic BESetOperation
    JSONObject parameters;
    try {
      JSONObject authenticationData = new JSONObject();
      authenticationData.put(authType, PointerEncoder.get().encode(authData));

      parameters = new JSONObject();
      parameters.put("authData", authenticationData);
    } catch (JSONException e) {
      throw new RuntimeException("could not serialize object to JSON");
    }

    return serviceLogInUserCommand(parameters, null, revocableSession);
  }

  public static BERESTUserCommand serviceLogInUserCommand(JSONObject parameters,
                                                             String sessionToken, boolean revocableSession) {
    return new BERESTUserCommand(
        "users", BEHttpRequest.Method.POST, parameters, sessionToken, revocableSession);
  }

  //endregion

  public static BERESTUserCommand resetPasswordResetCommand(String email) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("email", email);
    return new BERESTUserCommand(
        "requestPasswordReset", BEHttpRequest.Method.POST, parameters, null);
  }

  private boolean isRevocableSessionEnabled;
  private int statusCode;

  private BERESTUserCommand(
      String httpPath,
      BEHttpRequest.Method httpMethod,
      Map<String, ?> parameters,
      String sessionToken) {
    this(httpPath, httpMethod, parameters, sessionToken, false);
  }

  private BERESTUserCommand(
          String httpPath,
          BEHttpRequest.Method httpMethod,
          Map<String, ?> parameters,
          String sessionToken, boolean isRevocableSessionEnabled) {
    super(httpPath, httpMethod, parameters, sessionToken);
    this.isRevocableSessionEnabled = isRevocableSessionEnabled;
  }

  private BERESTUserCommand(
          String httpPath,
          BEHttpRequest.Method httpMethod,
          JSONObject parameters,
          String sessionToken, boolean isRevocableSessionEnabled) {
    super(httpPath, httpMethod, parameters, sessionToken);
    this.isRevocableSessionEnabled = isRevocableSessionEnabled;
  }

  public int getStatusCode() {
    return statusCode;
  }

  @Override
  protected void addAdditionalHeaders(BEHttpRequest.Builder requestBuilder) {
    super.addAdditionalHeaders(requestBuilder);
    if (isRevocableSessionEnabled) {
      requestBuilder.addHeader(HEADER_REVOCABLE_SESSION, HEADER_TRUE);
    }
  }

  @Override
  protected Task<JSONObject> onResponseAsync(BEHttpResponse response,
                                             ProgressCallback progressCallback) {
    statusCode = response.getStatusCode();
    return super.onResponseAsync(response, progressCallback);
  }

}
