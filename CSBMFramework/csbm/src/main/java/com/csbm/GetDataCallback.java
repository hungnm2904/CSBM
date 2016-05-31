
package com.csbm;

/**
 * A {@code GetDataCallback} is used to run code after a {@link BEFile} fetches its data on a
 * background thread.
 * <p/>
 * The easiest way to use a {@code GetDataCallback} is through an anonymous inner class. Override
 * the {@code done} function to specify what the callback should do after the fetch is complete.
 * The {@code done} function will be run in the UI thread, while the fetch happens in a
 * background thread. This ensures that the UI does not freeze while the fetch happens.
 * <p/>
 * <pre>
 * file.getDataInBackground(new GetDataCallback() {
 *   public void done(byte[] data, BEException e) {
 *     // ...
 *   }
 * });
 * </pre>
 */
public interface GetDataCallback extends BECallback2<byte[], BEException> {
  /**
   * Override this function with the code you want to run after the fetch is complete.
   *
   * @param data
   *          The data that was retrieved, or {@code null} if it did not succeed.
   * @param e
   *          The exception raised by the fetch, or {@code null} if it succeeded.
   */
  @Override
  public void done(byte[] data, BEException e);
}

