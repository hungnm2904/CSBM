
package com.csbm;

import java.util.List;

/**
 * A {@code FindCallback} is used to run code after a {@link BEQuery} is used to fetch a list of
 * {@link BEObject}s in a background thread.
 * <p/>
 * The easiest way to use a {@code FindCallback} is through an anonymous inner class. Override the
 * {@code done} function to specify what the callback should do after the fetch is complete.
 * The {@code done} function will be run in the UI thread, while the fetch happens in a
 * background thread. This ensures that the UI does not freeze while the fetch happens.
 * <p/>
 * For example, this sample code fetches all objects of class {@code "MyClass"}. It calls a
 * different function depending on whether the fetch succeeded or not.
 * <p/>
 * <pre>
 * BEQuery&lt;BEObject&gt; query = BEQuery.getQuery(&quot;MyClass&quot;);
 * query.findInBackground(new FindCallback&lt;BEObject&gt;() {
 *   public void done(List&lt;BEObject&gt; objects, BEException e) {
 *     if (e == null) {
 *       objectsWereRetrievedSuccessfully(objects);
 *     } else {
 *       objectRetrievalFailed();
 *     }
 *   }
 * });
 * </pre>
 */
public interface FindCallback<T extends BEObject> extends BECallback2<List<T>, BEException> {
  /**
   * Override this function with the code you want to run after the fetch is complete.
   * 
   * @param objects
   *          The objects that were retrieved, or null if it did not succeed.
   * @param e
   *          The exception raised by the save, or null if it succeeded.
   */
  @Override
  public void done(List<T> objects, BEException e);
}
