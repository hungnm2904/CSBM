package com.csbm;


import com.csbm.http.BEHttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

/** package */ class BERESTPushCommand extends BERESTCommand {

  /* package */ final static String KEY_CHANNELS = "channels";
  /* package */ final static String KEY_WHERE = "where";
  /* package */ final static String KEY_DEVICE_TYPE = "deviceType";
  /* package */ final static String KEY_EXPIRATION_TIME = "expiration_time";
  /* package */ final static String KEY_EXPIRATION_INTERVAL = "expiration_interval";
  /* package */ final static String KEY_DATA = "data";

  public BERESTPushCommand(
      String httpPath,
      BEHttpRequest.Method httpMethod,
      JSONObject parameters,
      String sessionToken) {
    super(httpPath, httpMethod, parameters, sessionToken);
  }

  public static BERESTPushCommand sendPushCommand(BEQuery.State<BEInstallation> query,
                                                     Set<String> targetChannels, String targetDeviceType, Long expirationTime,
                                                     Long expirationInterval, JSONObject payload, String sessionToken) {
    JSONObject parameters = new JSONObject();
    try {
      if (targetChannels != null) {
        parameters.put(KEY_CHANNELS, new JSONArray(targetChannels));
      } else {
        JSONObject whereJSON = null;
        if (query != null) {
          BEQuery.QueryConstraints where = query.constraints();
          whereJSON = (JSONObject) PointerEncoder.get().encode(where);
        }
        if (targetDeviceType != null) {
          whereJSON = new JSONObject();
          whereJSON.put(KEY_DEVICE_TYPE, targetDeviceType);
        }
        if (whereJSON == null) {
          // If there are no conditions set, then push to everyone by specifying empty query conditions.
          whereJSON = new JSONObject();
        }
        parameters.put(KEY_WHERE, whereJSON);
      }

      if (expirationTime != null) {
        parameters.put(KEY_EXPIRATION_TIME, expirationTime);
      } else if (expirationInterval != null) {
        parameters.put(KEY_EXPIRATION_INTERVAL, expirationInterval);
      }

      if (payload != null) {
        parameters.put(KEY_DATA, payload);
      }
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return new BERESTPushCommand("push", BEHttpRequest.Method.POST, parameters, sessionToken);
  }
}
