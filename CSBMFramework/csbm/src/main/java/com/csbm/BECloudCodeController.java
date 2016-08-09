package com.csbm;

import org.json.JSONObject;

import java.util.Map;

import bolts.Continuation;
import bolts.Task;

/** package */ class BECloudCodeController {

  /* package for test */ final BEHttpClient restClient;

  public BECloudCodeController(BEHttpClient restClient) {
    this.restClient = restClient;
  }

  public <T> Task<T> callFunctionInBackground(final String name,
                                              final Map<String, ?> params, String sessionToken) {
    BERESTCommand command = BERESTCloudCommand.callFunctionCommand(
        name,
        params,
        sessionToken);
    return command.executeAsync(restClient).onSuccess(new Continuation<JSONObject, T>() {
      @Override
      public T then(Task<JSONObject> task) throws Exception {
        @SuppressWarnings("unchecked")
        T result = (T) convertCloudResponse(task.getResult());
        return result;
      }
    });
  }

  /*
   * Decodes any CSBM data types in the result of the cloud function call.
   */
  /* package for test */ Object convertCloudResponse(Object result) {
    if (result instanceof JSONObject) {
      JSONObject jsonResult = (JSONObject)result;
      result = jsonResult.opt("result");
    }

    BEDecoder decoder = BEDecoder.get();
    Object finalResult = decoder.decode(result);
    if (finalResult != null) {
      return finalResult;
    }

    return result;
  }
}
