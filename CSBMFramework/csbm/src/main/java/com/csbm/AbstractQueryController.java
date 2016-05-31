package com.csbm;

import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by akela on 29/05/2016.
 * {@code AbstractBEQueryController} is an abstract implementation of
 * {@link BEQueryController}, which implements {@link BEQueryController#getFirstAsync}.
 */

/** package */ abstract class AbstractQueryController implements BEQueryController {

  @Override
  public <T extends BEObject> Task<T> getFirstAsync(BEQuery.State<T> state, BEUser user,
                                                       Task<Void> cancellationToken) {
    return findAsync(state, user, cancellationToken).continueWith(new Continuation<List<T>, T>() {
      @Override
      public T then(Task<List<T>> task) throws Exception {
        if (task.isFaulted()) {
          throw task.getError();
        }
        if (task.getResult() != null && task.getResult().size() > 0) {
          return task.getResult().get(0);
        }
        throw new BEException(BEException.OBJECT_NOT_FOUND, "no results found for query");
      }
    });
  }
}
