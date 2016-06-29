/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
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
