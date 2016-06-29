/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.csbm;


import com.csbm.http.BEHttpBody;
import com.csbm.http.BEHttpRequest;
import com.csbm.http.BEHttpResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import bolts.Task;

/**
 * A helper object to send requests to the server.
 */

/** package */ class BERESTCommand extends BERequest<JSONObject> {

//  /* package */ static final String HEADER_APPLICATION_ID = "X-Parse-Application-Id";
//  /* package */ static final String HEADER_CLIENT_KEY = "X-Parse-Client-Key";
//  /* package */ static final String HEADER_CLIENT_VERSION = "X-Parse-Client-Version";
//  /* package */ static final String HEADER_APP_BUILD_VERSION = "X-Parse-App-Build-Version";
//  /* package */ static final String HEADER_APP_DISPLAY_VERSION = "X-Parse-App-Display-Version";
//  /* package */ static final String HEADER_OS_VERSION = "X-Parse-OS-Version";
//
//  /* package */ static final String HEADER_INSTALLATION_ID = "X-Parse-Installation-Id";
//  /* package */ static final String USER_AGENT = "User-Agent";
//  private static final String HEADER_SESSION_TOKEN = "X-Parse-Session-Token";
//  private static final String HEADER_MASTER_KEY = "X-Parse-Master-Key";


  /* package */ static final String HEADER_APPLICATION_ID = "X-CSBM-Application-Id";
  /* package */ static final String HEADER_CLIENT_KEY = "X-CSBM-Client-Key";
  /* package */ static final String HEADER_CLIENT_VERSION = "X-CSBM-Client-Version";
  /* package */ static final String HEADER_APP_BUILD_VERSION = "X-CSBM-App-Build-Version";
  /* package */ static final String HEADER_APP_DISPLAY_VERSION = "X-CSBM-App-Display-Version";
  /* package */ static final String HEADER_OS_VERSION = "X-CSBM-OS-Version";

  /* package */ static final String HEADER_INSTALLATION_ID = "X-CSBM-Installation-Id";
  /* package */ static final String USER_AGENT = "User-Agent";
  private static final String HEADER_SESSION_TOKEN = "X-CSBM-Session-Token";
  private static final String HEADER_MASTER_KEY = "X-CSBM-Master-Key";


  private static final String PARAMETER_METHOD_OVERRIDE = "_method";

  // Set via CSBM.initialize(Configuration)
  /* package */ static URL server = null;

  private static LocalIdManager getLocalIdManager() {
    return BECorePlugins.getInstance().getLocalIdManager();
  }

  /* package */ static abstract class Init<T extends Init<T>> {
    private String sessionToken;
    private String installationId;
    public String masterKey;

    private BEHttpRequest.Method method = BEHttpRequest.Method.GET;
    private String httpPath;
    private JSONObject jsonParameters;

    private String operationSetUUID;
    private String localId;

    /* package */ abstract T self();

    public T sessionToken(String sessionToken) {
      this.sessionToken = sessionToken;
      return self();
    }

    public T installationId(String installationId) {
      this.installationId = installationId;
      return self();
    }

    public T masterKey(String masterKey) {
      this.masterKey = masterKey;
      return self();
    }

    public T method(BEHttpRequest.Method method) {
      this.method = method;
      return self();
    }

    public T httpPath(String httpPath) {
      this.httpPath = httpPath;
      return self();
    }

    public T jsonParameters(JSONObject jsonParameters) {
      this.jsonParameters = jsonParameters;
      return self();
    }

    public T operationSetUUID(String operationSetUUID) {
      this.operationSetUUID = operationSetUUID;
      return self();
    }

    public T localId(String localId) {
      this.localId = localId;
      return self();
    }
  }

  public static class Builder extends Init<Builder> {
    @Override
    /* package */ Builder self() {
      return this;
    }

    public BERESTCommand build() {
      return new BERESTCommand(this);
    }
  }

  // Headers
  private final String sessionToken;
  private String installationId;
  public String masterKey;

  /* package */ String httpPath;
  /* package */ final JSONObject jsonParameters;

  private String operationSetUUID;
  private String localId;

  public BERESTCommand(
      String httpPath,
      BEHttpRequest.Method httpMethod,
      Map<String, ?> parameters,
      String sessionToken) {
    this(
        httpPath,
        httpMethod,
        parameters != null ? (JSONObject) NoObjectsEncoder.get().encode(parameters) : null,
        sessionToken);
  }

  public BERESTCommand(
      String httpPath,
      BEHttpRequest.Method httpMethod,
      JSONObject jsonParameters,
      String sessionToken) {
    this(httpPath, httpMethod, jsonParameters, null, sessionToken);
  }

  private BERESTCommand(
          String httpPath,
          BEHttpRequest.Method httpMethod,
          JSONObject jsonParameters,
          String localId, String sessionToken) {
    super(httpMethod, createUrl(httpPath));

    this.httpPath = httpPath;
    this.jsonParameters = jsonParameters;
    this.localId = localId;
    this.sessionToken = sessionToken;
  }

  /* package */ BERESTCommand(Init<?> builder) {
    super(builder.method, createUrl(builder.httpPath));
    sessionToken = builder.sessionToken;
    installationId = builder.installationId;
    masterKey = builder.masterKey;

    httpPath = builder.httpPath;
    jsonParameters = builder.jsonParameters;
    operationSetUUID = builder.operationSetUUID;
    localId = builder.localId;
  }

  public static BERESTCommand fromJSONObject(JSONObject jsonObject) {
    String httpPath = jsonObject.optString("httpPath");
    BEHttpRequest.Method httpMethod =
        BEHttpRequest.Method.fromString(jsonObject.optString("httpMethod"));
    String sessionToken = jsonObject.optString("sessionToken", null);
    String localId = jsonObject.optString("localId", null);
    JSONObject jsonParameters = jsonObject.optJSONObject("parameters");

    return new BERESTCommand(httpPath, httpMethod, jsonParameters, localId, sessionToken);
  }

  // TODO(grantland): But we don't disable retries by default...
  /* package */ void enableRetrying() {
    setMaxRetries(DEFAULT_MAX_RETRIES);
  }

  private static String createUrl(String httpPath) {
    // We send all parameters for GET/HEAD/DELETE requests in a post body,
    // so no need to worry about query parameters here.
    if (httpPath == null) {
      return server.toString();
    }

    try {
      return new URL(server, httpPath).toString();
    } catch (MalformedURLException ex) {
      throw new RuntimeException(ex);
    }
  }

  protected void addAdditionalHeaders(BEHttpRequest.Builder requestBuilder) {
    if (installationId != null) {
      requestBuilder.addHeader(HEADER_INSTALLATION_ID, installationId);
    }
    if (sessionToken != null) {
      requestBuilder.addHeader(HEADER_SESSION_TOKEN, sessionToken);
    }
    if (masterKey != null) {
      requestBuilder.addHeader(HEADER_MASTER_KEY, masterKey);
    }
  }

  @Override
  protected BEHttpRequest newRequest(
      BEHttpRequest.Method method,
      String url,
      ProgressCallback uploadProgressCallback) {
    BEHttpRequest request;
    if (jsonParameters != null &&
        method != BEHttpRequest.Method.POST &&
        method != BEHttpRequest.Method.PUT) {
      // The request URI may be too long to include parameters in the URI.
      // To avoid this problem we send the parameters in a POST request json-encoded body
      // and add a http method override parameter in newBody.
      request = super.newRequest(BEHttpRequest.Method.POST, url, uploadProgressCallback);
    } else {
      request = super.newRequest(method, url, uploadProgressCallback);
    }
    BEHttpRequest.Builder requestBuilder = new BEHttpRequest.Builder(request);
    addAdditionalHeaders(requestBuilder);
    return requestBuilder.build();
  }

  @Override
  protected BEHttpBody newBody(ProgressCallback uploadProgressCallback) {
    if (jsonParameters == null) {
      String message = String.format("Trying to execute a %s command without body parameters.",
          method.toString());
      throw new IllegalArgumentException(message);
    }

    try {
      JSONObject parameters = jsonParameters;
      if (method == BEHttpRequest.Method.GET ||
          method == BEHttpRequest.Method.DELETE) {
        // The request URI may be too long to include parameters in the URI.
        // To avoid this problem we send the parameters in a POST request json-encoded body
        // and add a http method override parameter.
        parameters = new JSONObject(jsonParameters.toString());
        parameters.put(PARAMETER_METHOD_OVERRIDE, method.toString());
      }
      return new BEByteArrayHttpBody(parameters.toString(), "application/json");
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  @Override
  public Task<JSONObject> executeAsync(
      final BEHttpClient client,
      final ProgressCallback uploadProgressCallback,
      final ProgressCallback downloadProgressCallback,
      final Task<Void> cancellationToken) {
    resolveLocalIds();
    return super.executeAsync(
        client, uploadProgressCallback, downloadProgressCallback, cancellationToken);
  }

  @Override
  protected Task<JSONObject> onResponseAsync(BEHttpResponse response,
                                             ProgressCallback downloadProgressCallback) {
    String content;
    InputStream responseStream = null;
    try {
      responseStream = response.getContent();
      content = new String(BEIOUtils.toByteArray(responseStream));
    } catch (IOException e) {
      return Task.forError(e);
    } finally {
      BEIOUtils.closeQuietly(responseStream);
    }

    // We need to check for errors differently in /1/ than /2/ since object data in /2/ was
    // encapsulated in "data" and everything was 200, but /2/ everything is in the root JSON,
    // but errors are status 4XX.
    // See https://quip.com/4pbbA9HbOPjQ
    int statusCode = response.getStatusCode();
    if (statusCode >= 200 && statusCode < 600) { // Assume 3XX is handled by http library
      JSONObject json;
      try {
        json = new JSONObject(content);

        if (statusCode >= 400 && statusCode < 500) { // 4XX
          return Task.forError(newPermanentException(json.optInt("code"), json.optString("error")));
        } else if (statusCode >= 500) { // 5XX
          return Task.forError(newTemporaryException(json.optInt("code"), json.optString("error")));
        }

        return Task.forResult(json);
      } catch (JSONException e) {
        return Task.forError(newTemporaryException("bad json response", e));
      }
    }

    return Task.forError(newPermanentException(BEException.OTHER_CAUSE, content));
  }

  // Creates a somewhat-readable string that uniquely identifies this command.
  public String getCacheKey() {
    String json;
    if (jsonParameters != null) {
      try {
        json = toDeterministicString(jsonParameters);
      } catch (JSONException e) {
        throw new RuntimeException(e.getMessage());
      }
    } else {
      json = "";
    }

    // Include the session token in the cache in order to avoid mixing permissions.
    if (sessionToken != null) {
      json += sessionToken;
    }

    return String.format(
        "BERESTCommand.%s.%s.%s",
        method.toString(),
        BEDigestUtils.md5(httpPath),
        BEDigestUtils.md5(json)
    );
  }

  // Encodes the object to JSON, but ensures that JSONObjects
  // and nested JSONObjects are encoded with keys in alphabetical order.
  /** package */ static String toDeterministicString(Object o) throws JSONException {
    JSONStringer stringer = new JSONStringer();
    addToStringer(stringer, o);
    return stringer.toString();
  }

  // Uses the provided JSONStringer to encode this object to JSON, but ensures that JSONObjects and
  // nested JSONObjects are encoded with keys in alphabetical order.
  private static void addToStringer(JSONStringer stringer, Object o) throws JSONException {
    if (o instanceof JSONObject) {
      stringer.object();
      JSONObject object = (JSONObject) o;
      Iterator<String> keyIterator = object.keys();
      ArrayList<String> keys = new ArrayList<>();
      while (keyIterator.hasNext()) {
        keys.add(keyIterator.next());
      }
      Collections.sort(keys);

      for (String key : keys) {
        stringer.key(key);
        addToStringer(stringer, object.opt(key));
      }

      stringer.endObject();
      return;
    }

    if (o instanceof JSONArray) {
      JSONArray array = (JSONArray) o;
      stringer.array();
      for (int i = 0; i < array.length(); ++i) {
        addToStringer(stringer, array.get(i));
      }
      stringer.endArray();
      return;
    }

    stringer.value(o);
  }

  /* package */ static boolean isValidCommandJSONObject(JSONObject jsonObject) {
    return jsonObject.has("httpPath");
  }

  // This function checks whether a json object is a valid /2 BECommand json.
  /* package */ static boolean isValidOldFormatCommandJSONObject(JSONObject jsonObject) {
    return jsonObject.has("op");
  }

  public JSONObject toJSONObject() {
    JSONObject jsonObject = new JSONObject();
    try {
      if (httpPath != null) {
        jsonObject.put("httpPath", httpPath);
      }
      jsonObject.put("httpMethod", method.toString());
      if (jsonParameters != null) {
        jsonObject.put("parameters", jsonParameters);
      }
      if (sessionToken != null) {
        jsonObject.put("sessionToken", sessionToken);
      }
      if (localId != null) {
        jsonObject.put("localId", localId);
      }
    } catch (JSONException e) {
      throw new RuntimeException(e.getMessage());
    }
    return jsonObject;
  }

  public String getSessionToken() {
    return sessionToken;
  }

  public String getOperationSetUUID() {
    return operationSetUUID;
  }

  /* package */ void setOperationSetUUID(String operationSetUUID) {
    this.operationSetUUID = operationSetUUID;
  }

  public void setLocalId(String localId) {
    this.localId = localId;
  }

  public String getLocalId() {
    return localId;
  }


  /**
   * If this was the second save on a new object while offline, then its objectId wasn't yet set
   * when the command was created, so it would have been considered a "create". But if the first
   * save succeeded, then there is an objectId now, and it will be mapped to the localId for this
   * command's result. If so, change the "create" operation to an "update", and add the objectId to
   * the command.
   */
  private void maybeChangeServerOperation() throws JSONException {
    if (localId != null) {
      String objectId = getLocalIdManager().getObjectId(localId);
      if (objectId != null) {
        localId = null;
        httpPath += String.format("/%s", objectId);
        url = createUrl(httpPath);

        if (httpPath.startsWith("classes") && method == BEHttpRequest.Method.POST) {
          method = BEHttpRequest.Method.PUT;
        }
      }
    }
  }

  public void resolveLocalIds() {
    try {
      ArrayList<JSONObject> localPointers = new ArrayList<>();
      getLocalPointersIn(jsonParameters, localPointers);
      for (JSONObject pointer : localPointers) {
        String localId = (String) pointer.get("localId");
        String objectId = getLocalIdManager().getObjectId(localId);
        if (objectId == null) {
          throw new IllegalStateException(
              "Tried to serialize a command referencing a new, unsaved object.");
        }
        pointer.put("objectId", objectId);
        pointer.remove("localId");
      }
      maybeChangeServerOperation();
    } catch (JSONException e) {
      // Well, nothing to do here...
    }
  }

  /**
   * Finds all of the local ids in this command and increments their retain counts in the on-disk
   * store. This should be called immediately before serializing the command to disk, so that we
   * know we might need to resolve these local ids at some point in the future.
   */
  public void retainLocalIds() {
    if (localId != null) {
      getLocalIdManager().retainLocalIdOnDisk(localId);
    }

    try {
      ArrayList<JSONObject> localPointers = new ArrayList<>();
      getLocalPointersIn(jsonParameters, localPointers);
      for (JSONObject pointer : localPointers) {
        String localId = (String) pointer.get("localId");
        getLocalIdManager().retainLocalIdOnDisk(localId);
      }
    } catch (JSONException e) {
      // Well, nothing to do here...
    }
  }

  /**
   * Finds all of the local ids in this command and decrements their retain counts in the on-disk
   * store. This should be called when removing a serialized command from the disk, when we know
   * that we will never need to resolve these local ids for this command again in the future.
   */
  public void releaseLocalIds() {
    if (localId != null) {
      getLocalIdManager().releaseLocalIdOnDisk(localId);
    }
    try {
      ArrayList<JSONObject> localPointers = new ArrayList<>();
      getLocalPointersIn(jsonParameters, localPointers);
      for (JSONObject pointer : localPointers) {
        String localId = (String) pointer.get("localId");
        getLocalIdManager().releaseLocalIdOnDisk(localId);
      }
    } catch (JSONException e) {
      // Well, nothing to do here...
    }
  }

  protected static void getLocalPointersIn(Object container, ArrayList<JSONObject> localPointers)
      throws JSONException {
    if (container instanceof JSONObject) {
      JSONObject object = (JSONObject) container;
      if ("Pointer".equals(object.opt("__type")) && object.has("localId")) {
        localPointers.add((JSONObject) container);
        return;
      }

      Iterator<String> keyIterator = object.keys();
      while (keyIterator.hasNext()) {
        String key = keyIterator.next();
        getLocalPointersIn(object.get(key), localPointers);
      }
    }

    if (container instanceof JSONArray) {
      JSONArray array = (JSONArray) container;
      for (int i = 0; i < array.length(); ++i) {
        getLocalPointersIn(array.get(i), localPointers);
      }
    }
  }
}
