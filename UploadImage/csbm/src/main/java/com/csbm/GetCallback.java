package com.csbm;

/**
 * A {@code GetCallback} is used to run code after a {@link BEQuery} is used to fetch a
 * {@link BEObject} in a background thread.
 * <p/>
 * The easiest way to use a {@code GetCallback} is through an anonymous inner class. Override the
 * {@code done} function to specify what the callback should do after the fetch is complete.
 * The {@code done} function will be run in the UI thread, while the fetch happens in a
 * background thread. This ensures that the UI does not freeze while the fetch happens.
 * <p/>
 * For example, this sample code fetches an object of class {@code "MyClass"} and id
 * {@code myId}. It calls a different function depending on whether the fetch succeeded or not.
 * <p/>
 * <pre>
 * BEQuery&lt;BEObject&gt; query = BEQuery.getQuery(&quot;MyClass&quot;);
 * query.getInBackground(myId, new GetCallback&lt;BEObject&gt;() {
 *   public void done(BEObject object, BEException e) {
 *     if (e == null) {
 *       objectWasRetrievedSuccessfully(object);
 *     } else {
 *       objectRetrievalFailed();
 *     }
 *   }
 * });
 * </pre>
 */
public interface GetCallback<T extends BEObject> extends BECallback2<T, BEException> {
  /**
   * Override this function with the code you want to run after the fetch is complete.
   * 
   * @param object
   *          The object that was retrieved, or {@code null} if it did not succeed.
   * @param e
   *          The exception raised by the fetch, or {@code null} if it succeeded.
   */
  @Override
  public void done(T object, BEException e);
}
