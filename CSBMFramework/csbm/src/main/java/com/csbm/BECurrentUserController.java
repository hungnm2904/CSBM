package com.csbm;

import bolts.Task;

/** package */ interface BECurrentUserController
    extends BEObjectCurrentController<BEUser> {

  /**
   * Gets the persisted current BEUser.
   * @param shouldAutoCreateUser
   * @return
   */
  Task<BEUser> getAsync(boolean shouldAutoCreateUser);

  /**
   * Sets the persisted current BEUser only if it's current or we're not synced with disk.
   * @param user
   * @return
   */
  Task<Void> setIfNeededAsync(BEUser user);

  /**
   * Gets the session token of the persisted current BEUser.
   * @return
   */
  Task<String> getCurrentSessionTokenAsync();

  /**
   * Logs out the current BEUser.
   * @return
   */
  Task<Void> logOutAsync();
}
