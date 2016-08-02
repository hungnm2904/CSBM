package com.csbm;

/**
 * A {@code LogOutCallback} is used to run code after logging out a user.
 * <p/>
 * The easiest way to use a {@code LogOutCallback} is through an anonymous inner class. Override the
 * {@code done} function to specify what the callback should do after the login is complete.
 * The {@code done} function will be run in the UI thread, while the login happens in a
 * background thread. This ensures that the UI does not freeze while the save happens.
 * <p/>
 * For example, this sample code logs out a user and calls a different function depending on whether
 * the log out succeeded or not.
 * <p/>
 * <pre>
 * BEUser.logOutInBackground(new LogOutCallback() {
 *   public void done(BEException e) {
 *     if (e == null) {
 *       logOutSuccessful();
 *     } else {
 *       somethingWentWrong();
 *     }
 *   }
 * });
 * </pre>
 */
public interface LogOutCallback extends BECallback1<BEException> {
  /**
   * Override this function with the code you want to run after the save is complete.
   *
   * @param e
   *          The exception raised by the log out, or {@code null} if it succeeded.
   */
  @Override
  public void done(BEException e);
}
