package com.csbm;

/**
 * A {@code RequestPasswordResetCallback} is used to run code requesting a password reset for a
 * user.
 * <p/>
 * The easiest way to use a {@code RequestPasswordResetCallback} is through an anonymous inner
 * class. Override the {@code done} function to specify what the callback should do after the
 * request is complete. The {@code done} function will be run in the UI thread, while the request
 * happens in a background thread. This ensures that the UI does not freeze while the request
 * happens.
 * <p/>
 * For example, this sample code requests a password reset for a user and calls a different function
 * depending on whether the request succeeded or not.
 * <p/>
 * <pre>
 * BEUser.requestPasswordResetInBackground(&quot;forgetful@example.com&quot;,
 *     new RequestPasswordResetCallback() {
 *       public void done(BEException e) {
 *         if (e == null) {
 *           requestedSuccessfully();
 *         } else {
 *           requestDidNotSucceed();
 *         }
 *       }
 *     });
 * </pre>
 */
public interface RequestPasswordResetCallback extends BECallback1<BEException> {
  /**
   * Override this function with the code you want to run after the request is complete.
   * 
   * @param e
   *          The exception raised by the save, or {@code null} if no account is associated with the
   *          email address.
   */
  @Override
  public void done(BEException e);
}
