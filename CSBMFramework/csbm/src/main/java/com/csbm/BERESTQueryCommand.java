package com.csbm;


import com.csbm.http.BEHttpRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** package */ class BERESTQueryCommand extends BERESTCommand {

  public static <T extends BEObject> BERESTQueryCommand findCommand(
      BEQuery.State<T> state, String sessionToken) {
    String httpPath = String.format("classes/%s", state.className());
    Map<String, String> parameters = encode(state, false);
    return new BERESTQueryCommand(
        httpPath, BEHttpRequest.Method.GET, parameters, sessionToken);
  }

  public static <T extends BEObject> BERESTQueryCommand countCommand(
      BEQuery.State<T> state, String sessionToken) {
    String httpPath = String.format("classes/%s", state.className());
    Map<String, String> parameters = encode(state, true);
    return new BERESTQueryCommand(
        httpPath, BEHttpRequest.Method.GET, parameters, sessionToken);
  }

  /* package */ static <T extends BEObject> Map<String, String> encode(
      BEQuery.State<T> state, boolean count) {
    BEEncoder encoder = PointerEncoder.get();
    HashMap<String, String> parameters = new HashMap<>();
    List<String> order = state.order();
    if (!order.isEmpty()) {
      parameters.put("order", BETextUtils.join(",", order));
    }

    BEQuery.QueryConstraints conditions = state.constraints();
    if (!conditions.isEmpty()) {
      JSONObject encodedConditions =
          (JSONObject) encoder.encode(conditions);
      parameters.put("where", encodedConditions.toString());
    }

    // This is nullable since we allow unset selectedKeys as well as no selectedKeys
    Set<String> selectedKeys = state.selectedKeys();
    if (selectedKeys != null) {
      parameters.put("keys", BETextUtils.join(",", selectedKeys));
    }

    Set<String> includeds = state.includes();
    if (!includeds.isEmpty()) {
      parameters.put("include", BETextUtils.join(",", includeds));
    }

    if (count) {
      parameters.put("count", Integer.toString(1));
    } else {
      int limit = state.limit();
      if (limit >= 0) {
        parameters.put("limit", Integer.toString(limit));
      }

      int skip = state.skip();
      if (skip > 0) {
        parameters.put("skip", Integer.toString(skip));
      }
    }

    Map<String, Object> extraOptions = state.extraOptions();
    for (Map.Entry<String, Object> entry : extraOptions.entrySet()) {
      Object encodedExtraOptions = encoder.encode(entry.getValue());
      parameters.put(entry.getKey(), encodedExtraOptions.toString());
    }

    if (state.isTracingEnabled()) {
      parameters.put("trace", Integer.toString(1));
    }
    return parameters;
  }

  private BERESTQueryCommand(
      String httpPath,
      BEHttpRequest.Method httpMethod,
      Map<String, ?> parameters,
      String sessionToken) {
    super(httpPath, httpMethod, parameters, sessionToken);
  }
}
