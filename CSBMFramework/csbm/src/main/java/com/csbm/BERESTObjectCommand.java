package com.csbm;

import android.net.Uri;

import com.csbm.http.BEHttpRequest;

import org.json.JSONObject;

/** package */ class BERESTObjectCommand extends BERESTCommand {

  public BERESTObjectCommand(
      String httpPath,
      BEHttpRequest.Method httpMethod,
      JSONObject parameters,
      String sessionToken) {
    super(httpPath, httpMethod, parameters, sessionToken);
  }

  public static BERESTObjectCommand getObjectCommand(String objectId, String className,
                                                        String sessionToken) {
    String httpPath = String.format("classes/%s/%s", Uri.encode(className), Uri.encode(objectId));
    return new BERESTObjectCommand(httpPath, BEHttpRequest.Method.GET, null, sessionToken);
  }

  public static BERESTObjectCommand saveObjectCommand(
          BEObject.State state, JSONObject operations, String sessionToken) {
    if (state.objectId() == null) {
      return BERESTObjectCommand.createObjectCommand(
          state.className(),
          operations,
          sessionToken);
    } else {
      return BERESTObjectCommand.updateObjectCommand(
          state.objectId(),
          state.className(),
          operations,
          sessionToken);
    }
  }

  private static BERESTObjectCommand createObjectCommand(String className, JSONObject changes,
                                                            String sessionToken) {
    String httpPath = String.format("classes/%s", Uri.encode(className));
    return new BERESTObjectCommand(httpPath, BEHttpRequest.Method.POST, changes, sessionToken);
  }

  private static BERESTObjectCommand updateObjectCommand(String objectId, String className,
                                                            JSONObject changes, String sessionToken) {
    String httpPath = String.format("classes/%s/%s", Uri.encode(className), Uri.encode(objectId));
    return new BERESTObjectCommand(httpPath, BEHttpRequest.Method.PUT, changes, sessionToken);
  }

  public static BERESTObjectCommand deleteObjectCommand(
          BEObject.State state, String sessionToken) {
    String httpPath = String.format("classes/%s", Uri.encode(state.className()));
    String objectId = state.objectId();
    if (objectId != null) {
      httpPath += String.format("/%s", Uri.encode(objectId));
    }
    return new BERESTObjectCommand(httpPath, BEHttpRequest.Method.DELETE, null, sessionToken);
  }
}
