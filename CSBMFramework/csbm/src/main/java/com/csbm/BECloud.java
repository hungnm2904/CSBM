package com.csbm;

import java.util.List;
import java.util.Map;

import bolts.Continuation;
import bolts.Task;

/**
 * The BECloud class defines provides methods for interacting with CSMB Cloud Functions. A Cloud
 * Function can be called with {@link #callFunctionInBackground(String, Map, FunctionCallback)}
 * using a {@link FunctionCallback}. For example, this sample code calls the "validateGame" Cloud
 * Function and calls processResponse if the call succeeded and handleError if it failed.
 *
 * <pre>
 * BECloud.callFunctionInBackground("validateGame", parameters, new FunctionCallback<Object>() {
 *      public void done(Object object, BEException e) {
 *        if (e == null) {
 *          processResponse(object);
 *        } else {
 *          handleError();
 *        }
 *      }
 * }
 * </pre>
 *
 * Using the callback methods is usually preferred because the network operation will not block the
 * calling thread. However, in some cases it may be easier to use the
 * {@link #callFunction(String, Map)} call which do block the calling thread. For example, if your
 * application has already spawned a background task to perform work, that background task could use
 * the blocking calls and avoid the code complexity of callbacks.
 */
public final class BECloud {

  /* package for test */ static BECloudCodeController getCloudCodeController() {
    return BECorePlugins.getInstance().getCloudCodeController();
  }

  /**
   * Calls a cloud function in the background.
   *
   * @param name
   *          The cloud function to call.
   * @param params
   *          The parameters to send to the cloud function. This map can contain anything that could
   *          be placed in a BEObject except for BEObjects themselves.
   *
   * @return A Task that will be resolved when the cloud function has returned.
   */
  public static <T> Task<T> callFunctionInBackground(final String name,
      final Map<String, ?> params) {
    return BEUser.getCurrentSessionTokenAsync().onSuccessTask(new Continuation<String, Task<T>>() {
      @Override
      public Task<T> then(Task<String> task) throws Exception {
        String sessionToken = task.getResult();
        return getCloudCodeController().callFunctionInBackground(name, params, sessionToken);
      }
    });
  }

  /**
   * Calls a cloud function.
   *
   * @param name
   *          The cloud function to call.
   * @param params
   *          The parameters to send to the cloud function. This map can contain anything that could
   *          be placed in a BEObject except for BEObjects themselves.
   * @return The result of the cloud call. Result may be a @{link Map}&lt; {@link String}, ?&gt;,
   *         {@link BEObject}, {@link List}&lt;?&gt;, or any type that can be set as a field in a
   *         BEObject.
   * @throws BEException
   */
  public static <T> T callFunction(String name, Map<String, ?> params) throws BEException {
    return BETaskUtils.wait(BECloud.<T>callFunctionInBackground(name, params));
  }

  /**
   * Calls a cloud function in the background.
   *
   * @param name
   *          The cloud function to call.
   * @param params
   *          The parameters to send to the cloud function. This map can contain anything that could
   *          be placed in a BEObject except for BEObjects themselves.
   * @param callback
   *          The callback that will be called when the cloud function has returned.
   */
  public static <T> void callFunctionInBackground(String name, Map<String, ?> params,
                                                  FunctionCallback<T> callback) {
    BETaskUtils.callbackOnMainThreadAsync(
        BECloud.<T>callFunctionInBackground(name, params),
        callback);
  }

  private BECloud() {
    // do nothing
  }
}
