package com.csbm;

/**
 * A {@code CountCallback} is used to run code after a {@link BEQuery} is used to count objects
 * matching a query in a background thread.
 * <p/>
 * The easiest way to use a {@code CountCallback} is through an anonymous inner class. Override the
 * {@code done} function to specify what the callback should do after the count is complete.
 * The {@code done} function will be run in the UI thread, while the count happens in a
 * background thread. This ensures that the UI does not freeze while the fetch happens.
 * <p/>
 * For example, this sample code counts objects of class {@code "MyClass"}. It calls a
 * different function depending on whether the count succeeded or not.
 * <p/>
 * <pre>
 * BEQuery&lt;BEObject&gt; query = BEQuery.getQuery(&quot;MyClass&quot;);
 * query.countInBackground(new CountCallback() {
 *   public void done(int count, BEException e) {
 *     if (e == null) {
 *       objectsWereCountedSuccessfully(count);
 *     } else {
 *       objectCountingFailed();
 *     }
 *   }
 * });
 * </pre>
 */
// FYI, this does not extend BECallback2 since the first param is `int`, which can't be used
// in a generic.
public interface CountCallback {
  /**
   * Override this function with the code you want to run after the count is complete.
   * 
   * @param count
   *          The number of objects matching the query, or -1 if it failed.
   * @param e
   *          The exception raised by the count, or null if it succeeded.
   */
  public void done(int count, BEException e);
}