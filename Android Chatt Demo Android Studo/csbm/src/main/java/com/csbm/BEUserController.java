/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.csbm;

import java.util.Map;

import bolts.Task;

/** package */ interface BEUserController {

  Task<BEUser.State> signUpAsync(
          BEObject.State state,
          BEOperationSet operations,
          String sessionToken);

  //region logInAsync

  Task<BEUser.State> logInAsync(
          String username, String password);

  Task<BEUser.State> logInAsync(
          BEUser.State state, BEOperationSet operations);

  Task<BEUser.State> logInAsync(
          String authType, Map<String, String> authData);

  //endregion

  Task<BEUser.State> getUserAsync(String sessionToken);

  Task<Void> requestPasswordResetAsync(String email);
}
