package com.csbm;


import com.csbm.http.BEHttpRequest;
import com.csbm.http.BEHttpResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

/** package */ class BERESTObjectBatchCommand extends BERESTCommand {
  public final static int COMMAND_OBJECT_BATCH_MAX_SIZE = 50;

  private static final String KEY_RESULTS = "results";

  public static List<Task<JSONObject>> executeBatch(
          BEHttpClient client, List<BERESTObjectCommand> commands, String sessionToken) {
    final int batchSize = commands.size();
    List<Task<JSONObject>> tasks = new ArrayList<>(batchSize);

    if (batchSize == 1) {
      // There's only one, just execute it
      tasks.add(commands.get(0).executeAsync(client));
      return tasks;
    }

    if (batchSize > COMMAND_OBJECT_BATCH_MAX_SIZE) {
      // There's more than the max, split it up into batches
      List<List<BERESTObjectCommand>> batches = Lists.partition(commands,
          COMMAND_OBJECT_BATCH_MAX_SIZE);
      for (int i = 0, size = batches.size(); i < size; i++) {
        List<BERESTObjectCommand> batch = batches.get(i);
        tasks.addAll(executeBatch(client, batch, sessionToken));
      }
      return tasks;
    }

    final List<TaskCompletionSource<JSONObject>> tcss = new ArrayList<>(batchSize);
    for (int i = 0; i < batchSize; i++) {
      TaskCompletionSource<JSONObject> tcs = new TaskCompletionSource<>();
      tcss.add(tcs);
      tasks.add(tcs.getTask());
    }

    JSONObject parameters = new JSONObject();
    JSONArray requests = new JSONArray();
    try {
      for (BERESTObjectCommand command : commands) {
        JSONObject requestParameters = new JSONObject();
        requestParameters.put("method", command.method.toString());
        requestParameters.put("path", new URL(server, command.httpPath).getPath());
        JSONObject body = command.jsonParameters;
        if (body != null) {
          requestParameters.put("body", body);
        }
        requests.put(requestParameters);
      }
      parameters.put("requests", requests);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }

    BERESTCommand command = new BERESTObjectBatchCommand(
        "batch", BEHttpRequest.Method.POST, parameters, sessionToken);

    command.executeAsync(client).continueWith(new Continuation<JSONObject, Void>() {
      @Override
      public Void then(Task<JSONObject> task) throws Exception {
        TaskCompletionSource<JSONObject> tcs;

        if (task.isFaulted() || task.isCancelled()) {
          // REST command failed or canceled, fail or cancel all tasks
          for (int i = 0; i < batchSize; i++) {
            tcs = tcss.get(i);
            if (task.isFaulted()) {
              tcs.setError(task.getError());
            } else {
              tcs.setCancelled();
            }
          }
        }

        JSONObject json = task.getResult();
        JSONArray results = json.getJSONArray(KEY_RESULTS);

        int resultLength = results.length();
        if (resultLength != batchSize) {
          // Invalid response, fail all tasks
          for (int i = 0; i < batchSize; i++) {
            tcs = tcss.get(i);
            tcs.setError(new IllegalStateException(
                "Batch command result count expected: " + batchSize + " but was: " + resultLength));
          }
        }

        for (int i = 0; i < batchSize; i++) {
          JSONObject result = results.getJSONObject(i);
          tcs = tcss.get(i);

          if (result.has("success")) {
            JSONObject success = result.getJSONObject("success");
            tcs.setResult(success);
          } else if (result.has("error")) {
            JSONObject error = result.getJSONObject("error");
            tcs.setError(new BEException(error.getInt("code"), error.getString("error")));
          }
        }
        return null;
      }
    });

    return tasks;
  }

  private BERESTObjectBatchCommand(
      String httpPath,
      BEHttpRequest.Method httpMethod,
      JSONObject parameters,
      String sessionToken) {
    super(httpPath, httpMethod, parameters, sessionToken);
  }

  /**
   * /batch is the only endpoint that doesn't return a JSONObject... It returns a JSONArray, but
   * let's wrap that with a JSONObject {@code { "results": &lt;original response%gt; }}.
   */
  @Override
  protected Task<JSONObject> onResponseAsync(BEHttpResponse response,
                                             ProgressCallback downloadProgressCallback) {
    InputStream responseStream = null;
    String content = null;
    try {
      responseStream = response.getContent();
      content = new String(BEIOUtils.toByteArray(responseStream));
    } catch (IOException e) {
      return Task.forError(e);
    } finally {
      BEIOUtils.closeQuietly(responseStream);
    }

    JSONObject json;
    try {
      JSONArray results = new JSONArray(content);
      json = new JSONObject();
      json.put(KEY_RESULTS, results);
    } catch (JSONException e) {
      return Task.forError(newTemporaryException("bad json response", e));
    }

    return Task.forResult(json);
  }
}
