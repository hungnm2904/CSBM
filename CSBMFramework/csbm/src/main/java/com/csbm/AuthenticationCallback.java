package com.csbm;

import java.util.Map;

/**
 * Created by akela on 29/05/2016.
 * Provides a general interface for delegation of third party authentication callbacks.
 */
public interface AuthenticationCallback {
  /**
   * Called when restoring third party authentication credentials that have been serialized,
   * such as session keys, etc.
   * <p />
   * <strong>Note:</strong> This will be executed on a background thread.
   *
   * @param authData
   *          The auth data for the provider. This value may be {@code null} when
   *          unlinking an account.
   *
   * @return {@code true} iff the {@code authData} was successfully synchronized or {@code false}
   *          if user should no longer be associated because of bad {@code authData}.
   */
  boolean onRestore(Map<String, String> authData);
}
