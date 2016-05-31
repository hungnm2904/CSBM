/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.csbm;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * The {@code BESession} is a local representation of session data that can be saved
 * and retrieved from the CSBM cloud.
 */
@BEClassName("_Session")
public class BESession extends BEObject {

  private static final String KEY_SESSION_TOKEN = "sessionToken";
  private static final String KEY_CREATED_WITH = "createdWith";
  private static final String KEY_RESTRICTED = "restricted";
  private static final String KEY_USER = "user";
  private static final String KEY_EXPIRES_AT = "expiresAt";
  private static final String KEY_INSTALLATION_ID = "installationId";

  private static final List<String> READ_ONLY_KEYS = Collections.unmodifiableList(
      Arrays.asList(KEY_SESSION_TOKEN, KEY_CREATED_WITH, KEY_RESTRICTED, KEY_USER, KEY_EXPIRES_AT,
          KEY_INSTALLATION_ID));

  private static BESessionController getSessionController() {
    return BECorePlugins.getInstance().getSessionController();
  }

  /**
   * Get the current {@code BESession} object related to the current user.
   *
   * @return A task that resolves a {@code BESession} object or {@code null} if not valid or
   *         logged in.
   */
  public static Task<BESession> getCurrentSessionInBackground() {
    return BEUser.getCurrentSessionTokenAsync().onSuccessTask(new Continuation<String, Task<BESession>>() {
      @Override
      public Task<BESession> then(Task<String> task) throws Exception {
        String sessionToken = task.getResult();
        if (sessionToken == null) {
          return Task.forResult(null);
        }
        return getSessionController().getSessionAsync(sessionToken).onSuccess(new Continuation<BEObject.State, BESession>() {
          @Override
          public BESession then(Task<BEObject.State> task) throws Exception {
            BEObject.State result = task.getResult();
            return BEObject.from(result);
          }
        });
      }
    });
  }

  /**
   * Get the current {@code BESession} object related to the current user.
   *
   * @param callback A callback that returns a {@code BESession} object or {@code null} if not
   *                 valid or logged in.
   */
  public static void getCurrentSessionInBackground(GetCallback<BESession> callback) {
    BETaskUtils.callbackOnMainThreadAsync(getCurrentSessionInBackground(), callback);
  }

  /* package */ static Task<Void> revokeAsync(String sessionToken) {
    if (sessionToken == null || !isRevocableSessionToken(sessionToken)) {
      return Task.forResult(null);
    }
    return getSessionController().revokeAsync(sessionToken);
  }

  /* package */ static Task<String> upgradeToRevocableSessionAsync(String sessionToken) {
    if (sessionToken == null || isRevocableSessionToken(sessionToken)) {
      return Task.forResult(sessionToken);
    }

    return getSessionController().upgradeToRevocable(sessionToken).onSuccess(new Continuation<BEObject.State, String>() {
      @Override
      public String then(Task<BEObject.State> task) throws Exception {
        BEObject.State result = task.getResult();
        return BEObject.<BESession>from(result).getSessionToken();
      }
    });
  }

  /* package */ static boolean isRevocableSessionToken(String sessionToken) {
    return sessionToken.contains("r:");
  }

  /**
   * Constructs a query for {@code BESession}.
   *
   * @see com.csbm.BEQuery#getQuery(Class)
   */
  public static BEQuery<BESession> getQuery() {
    return BEQuery.getQuery(BESession.class);
  }

  @Override
  /* package */ boolean needsDefaultACL() {
    return false;
  }

  @Override
  /* package */ boolean isKeyMutable(String key) {
    return !READ_ONLY_KEYS.contains(key);
  }

  /**
   * @return the session token for a user, if they are logged in.
   */
  public String getSessionToken() {
    return getString(KEY_SESSION_TOKEN);
  }
}
