package com.csbm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/** package */ class NetworkQueryController extends AbstractQueryController {

  private static final String TAG = "NetworkQueryController";

  private final BEHttpClient restClient;

  public NetworkQueryController(BEHttpClient restClient) {
    this.restClient = restClient;
  }

  @Override
  public <T extends BEObject> Task<List<T>> findAsync(
          BEQuery.State<T> state, BEUser user, Task<Void> cancellationToken) {
    String sessionToken = user != null ? user.getSessionToken() : null;
    return findAsync(state, sessionToken, true, cancellationToken);
  }

  @Override
  public <T extends BEObject> Task<Integer> countAsync(
          BEQuery.State<T> state, BEUser user, Task<Void> cancellationToken) {
    String sessionToken = user != null ? user.getSessionToken() : null;
    return countAsync(state, sessionToken, true, cancellationToken);
  }

  /**
   * Retrieves a list of {@link BEObject}s that satisfy this query from the source.
   *
   * @return A list of all {@link BEObject}s obeying the conditions set in this query.
   */
  /* package */ <T extends BEObject> Task<List<T>> findAsync(
      final BEQuery.State<T> state,
      String sessionToken,
      boolean shouldRetry,
      Task<Void> ct) {
    final long queryStart = System.nanoTime();

    final BERESTCommand command = BERESTQueryCommand.findCommand(state, sessionToken);
    if (shouldRetry) {
      command.enableRetrying();
    }

    final long querySent = System.nanoTime();
    return command.executeAsync(restClient, ct).onSuccess(new Continuation<JSONObject, List<T>>() {
      @Override
      public List<T> then(Task<JSONObject> task) throws Exception {
        JSONObject json = task.getResult();
        // Cache the results, unless we are ignoring the cache
        BEQuery.CachePolicy policy = state.cachePolicy();
        if (policy != null && (policy != BEQuery.CachePolicy.IGNORE_CACHE)) {
          BEKeyValueCache.saveToKeyValueCache(command.getCacheKey(), json.toString());
        }

        long queryReceived = System.nanoTime();

        List<T> response = convertFindResponse(state, task.getResult());

        long objectsBE = System.nanoTime();

        if (json.has("trace")) {
          Object serverTrace = json.get("trace");
          PLog.d("BEQuery",
              String.format("Query pre-processing took %f seconds\n" +
                      "%s\n" +
                      "Client side parsing took %f seconds\n",
                  (querySent - queryStart) / (1000.0f * 1000.0f),
                  serverTrace,
                  (objectsBE - queryReceived) / (1000.0f * 1000.0f)));
        }
        return response;
      }
    }, Task.BACKGROUND_EXECUTOR);
  }

  /* package */ <T extends BEObject> Task<Integer> countAsync(
      final BEQuery.State<T> state,
      String sessionToken,
      boolean shouldRetry,
      Task<Void> ct) {
    final BERESTCommand command = BERESTQueryCommand.countCommand(state, sessionToken);
    if (shouldRetry) {
      command.enableRetrying();
    }

    return command.executeAsync(restClient, ct).onSuccessTask(new Continuation<JSONObject, Task<JSONObject>>() {
      @Override
      public Task<JSONObject> then(Task<JSONObject> task) throws Exception {
        // Cache the results, unless we are ignoring the cache
        BEQuery.CachePolicy policy = state.cachePolicy();
        if (policy != null && policy != BEQuery.CachePolicy.IGNORE_CACHE) {
          JSONObject result = task.getResult();
          BEKeyValueCache.saveToKeyValueCache(command.getCacheKey(), result.toString());
        }
        return task;
      }
    }, Task.BACKGROUND_EXECUTOR).onSuccess(new Continuation<JSONObject, Integer>() {
      @Override
      public Integer then(Task<JSONObject> task) throws Exception {
        // Convert response
        return task.getResult().optInt("count");
      }
    });
  }

  // Converts the JSONArray that represents the results of a find command to an
  // ArrayList<BEObject>.
  /* package */ <T extends BEObject> List<T> convertFindResponse(BEQuery.State<T> state,
                                                                    JSONObject response) throws JSONException {
    ArrayList<T> answer = new ArrayList<>();
    JSONArray results = response.getJSONArray("results");
    if (results == null) {
      PLog.d(TAG, "null results in find response");
    } else {
      String resultClassName = response.optString("className", null);
      if (resultClassName == null) {
        resultClassName = state.className();
      }
      for (int i = 0; i < results.length(); ++i) {
        JSONObject data = results.getJSONObject(i);
        T object = BEObject.fromJSON(data, resultClassName, state.selectedKeys() == null);
        answer.add(object);

        /*
         * If there was a $relatedTo constraint on the query, then add any results to the list of
         * known objects in the relation for offline caching
         */
        BEQuery.RelationConstraint relation =
            (BEQuery.RelationConstraint) state.constraints().get("$relatedTo");
        if (relation != null) {
          relation.getRelation().addKnownObject(object);
        }
      }
    }

    return answer;
  }
}
