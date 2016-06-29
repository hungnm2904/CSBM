package com.csbm;

import java.util.List;

import bolts.Task;

/** package */ class OfflineQueryController extends AbstractQueryController {

  private final OfflineStore offlineStore;
  private final BEQueryController networkController;

  public OfflineQueryController(OfflineStore store, BEQueryController network) {
    offlineStore = store;
    networkController = network;
  }

  @Override
  public <T extends BEObject> Task<List<T>> findAsync(
          BEQuery.State<T> state,
          BEUser user,
      Task<Void> cancellationToken) {
    if (state.isFromLocalDatastore()) {
      return offlineStore.findFromPinAsync(state.pinName(), state, user);
    } else {
      return networkController.findAsync(state, user, cancellationToken);
    }
  }

  @Override
  public <T extends BEObject> Task<Integer> countAsync(
          BEQuery.State<T> state,
          BEUser user,
      Task<Void> cancellationToken) {
    if (state.isFromLocalDatastore()) {
      return offlineStore.countFromPinAsync(state.pinName(), state, user);
    } else {
      return networkController.countAsync(state, user, cancellationToken);
    }
  }
}
