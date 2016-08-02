/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.csbm;

import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;

/** package */ class BEConfigController {

  private BECurrentConfigController currentConfigController;
  private final BEHttpClient restClient;

  public BEConfigController(BEHttpClient restClient,
                            BECurrentConfigController currentConfigController) {
    this.restClient = restClient;
    this.currentConfigController = currentConfigController;
  }
  /* package */ BECurrentConfigController getCurrentConfigController() {
    return currentConfigController;
  }

  public Task<BEConfig> getAsync(String sessionToken) {
    final BERESTCommand command = BERESTConfigCommand.fetchConfigCommand(sessionToken);
    command.enableRetrying();
    return command.executeAsync(restClient).onSuccessTask(new Continuation<JSONObject, Task<BEConfig>>() {
      @Override
      public Task<BEConfig> then(Task<JSONObject> task) throws Exception {
        JSONObject result = task.getResult();

        final BEConfig config = BEConfig.decode(result, BEDecoder.get());
        return currentConfigController.setCurrentConfigAsync(config).continueWith(new Continuation<Void, BEConfig>() {
          @Override
          public BEConfig then(Task<Void> task) throws Exception {
            return config;
          }
        });
      }
    });
  }
}
