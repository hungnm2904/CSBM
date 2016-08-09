package com.csbm;

/**
 * A {@code SaveCallback} is used to run code after saving a {@link BEObject} in a background
 * thread.
 * <p/>
 * The easiest way to use a {@code SaveCallback} is through an anonymous inner class. Override the
 * {@code done} function to specify what the callback should do after the save is complete. The
 * {@code done} function will be run in the UI thread, while the save happens in a background
 * thread. This ensures that the UI does not freeze while the save happens.
 * <p/>
 * For example, this sample code saves the object {@code myObject} and calls a different
 * function depending on whether the save succeeded or not.
 * <p/>
 * <pre>
 * myObject.saveInBackground(new SaveCallback() {
 *   public void done(BEException e) {
 *     if (e == null) {
 *       myObjectSavedSuccessfully();
 *     } else {
 *       myObjectSaveDidNotSucceed();
 *     }
 *   }
 * });
 * </pre>
 */
public interface SaveCallback extends BECallback1<BEException> {
  /**
   * Override this function with the code you want to run after the save is complete.
   * 
   * @param e
   *          The exception raised by the save, or {@code null} if it succeeded.
   */
  @Override
  public void done(BEException e);
}
