package com.csbm;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

/** package */ class CacheQueryController extends AbstractQueryController {

  private final NetworkQueryController networkController;

  public CacheQueryController(NetworkQueryController network) {
    networkController = network;
  }

  @Override
  public <T extends BEObject> Task<List<T>> findAsync(
      final BEQuery.State<T> state,
      final BEUser user,
      final Task<Void> cancellationToken) {
    final String sessionToken = user != null ? user.getSessionToken() : null;
    CommandDelegate<List<T>> callbacks = new CommandDelegate<List<T>>() {
      @Override
      public Task<List<T>> runOnNetworkAsync(boolean retry) {
        return networkController.findAsync(state, sessionToken, retry, cancellationToken);
      }

      @Override
      public Task<List<T>> runFromCacheAsync() {
        return findFromCacheAsync(state, sessionToken);
      }
    };
    return runCommandWithPolicyAsync(callbacks, state.cachePolicy());
  }

  @Override
  public <T extends BEObject> Task<Integer> countAsync(
      final BEQuery.State<T> state,
      final BEUser user,
      final Task<Void> cancellationToken) {
    final String sessionToken = user != null ? user.getSessionToken() : null;
    CommandDelegate<Integer> callbacks = new CommandDelegate<Integer>() {
      @Override
      public Task<Integer> runOnNetworkAsync(boolean retry) {
        return networkController.countAsync(state, sessionToken, retry, cancellationToken);
      }

      @Override
      public Task<Integer> runFromCacheAsync() {
        return countFromCacheAsync(state, sessionToken);
      }
    };
    return runCommandWithPolicyAsync(callbacks, state.cachePolicy());
  }

  /**
   * Retrieves the results of the last time {@link BEQuery#find()} was called on a query
   * identical to this one.
   *
   * @param sessionToken The user requesting access.
   * @return A list of {@link BEObject}s corresponding to this query. Returns null if there is no
   *          cache for this query.
   */
  private <T extends BEObject> Task<List<T>> findFromCacheAsync(
      final BEQuery.State<T> state, String sessionToken) {
    final String cacheKey = BERESTQueryCommand.findCommand(state, sessionToken).getCacheKey();
    return Task.call(new Callable<List<T>>() {
      @Override
      public List<T> call() throws Exception {
        JSONObject cached = BEKeyValueCache.jsonFromKeyValueCache(cacheKey, state.maxCacheAge());
        if (cached == null) {
          throw new BEException(BEException.CACHE_MISS, "results not cached");
        }
        try {
          return networkController.convertFindResponse(state, cached);
        } catch (JSONException e) {
          throw new BEException(BEException.CACHE_MISS, "the cache contains corrupted json");
        }
      }
    }, Task.BACKGROUND_EXECUTOR);
  }

  /**
   * Retrieves the results of the last time {@link BEQuery#count()} was called on a query
   * identical to this one.
   *
   * @param sessionToken The user requesting access.
   * @return A list of {@link BEObject}s corresponding to this query. Returns null if there is no
   *          cache for this query.
   */
  private <T extends BEObject> Task<Integer> countFromCacheAsync(
      final BEQuery.State<T> state, String sessionToken) {
    final String cacheKey = BERESTQueryCommand.countCommand(state, sessionToken).getCacheKey();
    return Task.call(new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        JSONObject cached = BEKeyValueCache.jsonFromKeyValueCache(cacheKey, state.maxCacheAge());
        if (cached == null) {
          throw new BEException(BEException.CACHE_MISS, "results not cached");
        }
        try {
          return cached.getInt("count");
        } catch (JSONException e) {
          throw new BEException(BEException.CACHE_MISS, "the cache contains corrupted json");
        }
      }
    }, Task.BACKGROUND_EXECUTOR);
  }

  private <TResult> Task<TResult> runCommandWithPolicyAsync(final CommandDelegate<TResult> c,
      BEQuery.CachePolicy policy) {
    switch (policy) {
      case IGNORE_CACHE:
      case NETWORK_ONLY:
        return c.runOnNetworkAsync(true);
      case CACHE_ONLY:
        return c.runFromCacheAsync();
      case CACHE_ELSE_NETWORK:
        return c.runFromCacheAsync().continueWithTask(new Continuation<TResult, Task<TResult>>() {
          @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
          @Override
          public Task<TResult> then(Task<TResult> task) throws Exception {
            if (task.getError() instanceof BEException) {
              return c.runOnNetworkAsync(true);
            }
            return task;
          }
        });
      case NETWORK_ELSE_CACHE:
        return c.runOnNetworkAsync(false).continueWithTask(new Continuation<TResult, Task<TResult>>() {
          @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
          @Override
          public Task<TResult> then(Task<TResult> task) throws Exception {
            Exception error = task.getError();
            if (error instanceof BEException &&
                ((BEException) error).getCode() == BEException.CONNECTION_FAILED) {
              return c.runFromCacheAsync();
            }
            // Either the query succeeded, or there was an an error with the query, not the
            // network
            return task;
          }
        });
      case CACHE_THEN_NETWORK:
        throw new RuntimeException(
            "You cannot use the cache policy CACHE_THEN_NETWORK with find()");
      default:
        throw new RuntimeException("Unknown cache policy: " + policy);
    }
  }

  /**
   * A callback that will be used to tell runCommandWithPolicy how to perform the command on the
   * network and form the cache.
   */
  private interface CommandDelegate<T> {
    // Fetches data from the network.
    Task<T> runOnNetworkAsync(boolean retry);

    // Fetches data from the cache.
    Task<T> runFromCacheAsync();
  }
}
