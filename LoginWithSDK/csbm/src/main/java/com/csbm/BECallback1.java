package com.csbm;

/**
 * A {@code BECallback} is used to do something after a background task completes. End users will
 * use a specific subclass of {@code BECallback}.
 */
/** package */ interface BECallback1<T extends Throwable> {
  /**
   * {@code done(t)} must be overridden when you are doing a background operation. It is called
   * when the background operation completes.
   * <p/>
   * If the operation is successful, {@code t} will be {@code null}.
   * <p/>
   * If the operation was unsuccessful, {@code t} will contain information about the operation
   * failure.
   *
   * @param t
   *          Generally an {@link Throwable} that was thrown by the operation, if there was any.
   */
  public void done(T t);
}
