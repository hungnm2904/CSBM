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

/** package */ interface BESessionController {

  Task<BEObject.State> getSessionAsync(String sessionToken);

  Task<Void> revokeAsync(String sessionToken);

  Task<BEObject.State> upgradeToRevocable(String sessionToken);
}
