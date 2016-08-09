package com.csbm;

/**
 * A {@code ProgressCallback} is used to get upload or download progress of a {@link BEFile}
 * action.
 * <p/>
 * The easiest way to use a {@code ProgressCallback} is through an anonymous inner class.
 */
// FYI, this does not extend BECallback2 since it does not match the usual signature
// done(T, BEException), but is done(T).
public interface ProgressCallback {
  /**
   * Override this function with your desired callback.
   */
  public void done(Integer percentDone);
}
