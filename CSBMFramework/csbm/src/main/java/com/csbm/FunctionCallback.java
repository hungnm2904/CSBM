package com.csbm;

/**
 * A {@code FunctionCallback} is used to run code after {@link BECloud#callFunction} is used to
 * run a Cloud Function in a background thread.
 * <p/>
 * The easiest way to use a {@code FunctionCallback} is through an anonymous inner class. Override
 * the {@code done} function to specify what the callback should do after the cloud function is
 * complete. The {@code done} function will be run in the UI thread, while the fetch happens in
 * a background thread. This ensures that the UI does not freeze while the fetch happens.
 * <p/>
 * For example, this sample code calls a cloud function {@code "MyFunction"} with
 * {@code params} and calls a different function depending on whether the function succeeded.
 * <p/>
 * <pre>
 * BECloud.callFunctionInBackground(&quot;MyFunction&quot;new, params, FunctionCallback<BEObject>() {
 *   public void done(BEObject object, BEException e) {
 *     if (e == null) {
 *       cloudFunctionSucceeded(object);
 *     } else {
 *       cloudFunctionFailed();
 *     }
 *   }
 * });
 * </pre>
 * 
 * @param <T>
 *          The type of object returned by the Cloud Function.
 */
public interface FunctionCallback<T> extends BECallback2<T, BEException> {
  /**
   * Override this function with the code you want to run after the cloud function is complete.
   * 
   * @param object
   *          The object that was returned by the cloud function.
   * @param e
   *          The exception raised by the cloud call, or {@code null} if it succeeded.
   */
  @Override
  public void done(T object, BEException e);
}
