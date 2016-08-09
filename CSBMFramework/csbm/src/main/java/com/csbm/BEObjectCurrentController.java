package com.csbm;

import bolts.Task;

/** package */ interface BEObjectCurrentController<T extends BEObject> {

  /**
   * Persist the currentBEObject
   * @param object
   * @return
   */
  Task<Void> setAsync(T object);

  /**
   * Get the persisted currentBEObject
   * @return
   */
  Task<T> getAsync();

  /**
   * Check whether the currentBEObject exists or not
   * @return
   */
  Task<Boolean> existsAsync();

  /**
   * Judge whether the given BEObject is the currentBEObject
   * @param object
   * @return {@code true} if the give {@link BEObject} is the currentBEObject
   */
  boolean isCurrent(T object);

  /**
   * A test helper to reset the current BEObject. This method nullifies the in memory
   * currentBEObject
   */
  void clearFromMemory();

  /**
   * A test helper to reset the current BEObject. This method nullifies the in memory and in
   * disk currentBEObject
   */
  void clearFromDisk();
}
