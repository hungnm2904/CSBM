package com.csbm;

/**
 * A {@code ConfigCallback} is used to run code after {@link BEConfig#getInBackground()} is used
 * to fetch a new configuration object from the server in a background thread.
 * <p>
 * The easiest way to use a {@code ConfigCallback} is through an anonymous inner class. Override the
 * {@code done} function to specify what the callback should do after the fetch is complete.
 * The {@code done} function will be run in the UI thread, while the fetch happens in a
 * background thread. This ensures that the UI does not freeze while the fetch happens.
 * <p/>
 * <pre>
 * BEConfig.getInBackground(new ConfigCallback() {
 *   public void done(BEConfig config, BEException e) {
 *     if (e == null) {
 *       configFetchSuccess(object);
 *     } else {
 *       configFetchFailed(e);
 *     }
 *   }
 * });
 * </pre>
 */
public interface ConfigCallback extends BECallback2<BEConfig, BEException> {
  /**
   * Override this function with the code you want to run after the fetch is complete.
   *
   * @param config
   *          A new {@code BEConfig} instance from the server, or {@code null} if it did not
   *          succeed.
   * @param e
   *          The exception raised by the fetch, or {@code null} if it succeeded.
   */
  @Override
  public void done(BEConfig config, BEException e);
}
