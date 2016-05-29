/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.csbm;

import java.util.List;

import bolts.Task;

/**
 * A {@code BEQueryController} defines how a {@link BEQuery} is executed.
 */

/** package */ interface BEQueryController {

  /**
   * Executor for {@code find} queries.
   * @param state Immutable query state to execute.
   * @param user The user executing the query that can be used to match ACLs.
   * @param cancellationToken Cancellation token.
   * @return A {@link Task} that resolves to the results of the find.
   */
  public <T extends BEObject> Task<List<T>> findAsync(BEQuery.State<T> state, BEUser user,
                                                         Task<Void> cancellationToken);

  /**
   * Executor for {@code count} queries.
   * @param state Immutable query state to execute.
   * @param user The user executing the query that can be used to match ACLs.
   * @param cancellationToken Cancellation token.
   * @return A {@link Task} that resolves to the results of the count.
   */
  public <T extends BEObject> Task<Integer> countAsync(BEQuery.State<T> state, BEUser user,
                                                          Task<Void> cancellationToken);

  /**
   * Executor for {@code getFirst} queries.
   * @param state Immutable query state to execute.
   * @param user The user executing the query that can be used to match ACLs.
   * @param cancellationToken Cancellation token.
   * @return A {@link Task} that resolves to the the first result of the query if successful and
   * there is at least one result or {@link BEException#OBJECT_NOT_FOUND} if there are no
   * results.
   */
  public <T extends BEObject> Task<T> getFirstAsync(BEQuery.State<T> state, BEUser user,
                                                       Task<Void> cancellationToken);
}
