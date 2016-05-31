/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.csbm;

import com.csbm.http.BEHttpRequest;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import bolts.Continuation;
import bolts.Task;

// TODO(grantland): Create BEFileController interface

/** package */ class BEFileController {

  private final Object lock = new Object();
  private final BEHttpClient restClient;
  private final File cachePath;

  private BEHttpClient awsClient;

  public BEFileController(BEHttpClient restClient, File cachePath) {
    this.restClient = restClient;
    this.cachePath = cachePath;
  }

  /**
   * Gets the AWS http client if exists, otherwise lazily creates since developers might not always
   * use our download mechanism.
   */
  /* package */ BEHttpClient awsClient() {
    synchronized (lock) {
      if (awsClient == null) {
        awsClient = BEPlugins.get().newHttpClient();
      }
      return awsClient;
    }
  }

  /* package for tests */ BEFileController awsClient(BEHttpClient awsClient) {
    synchronized (lock) {
      this.awsClient = awsClient;
    }
    return this;
  }

  public File getCacheFile(BEFile.State state) {
    return new File(cachePath, state.name());
  }

  /* package for tests */ File getTempFile(BEFile.State state) {
    if (state.url() == null) {
      return null;
    }
    return new File(cachePath, state.url() + ".tmp");
  }

  public boolean isDataAvailable(BEFile.State state) {
    return getCacheFile(state).exists();
  }

  public void clearCache() {
    File[] files = cachePath.listFiles();
    if (files == null) {
      return;
    }
    for (File file : files) {
      BEFileUtils.deleteQuietly(file);
    }
  }

  public Task<BEFile.State> saveAsync(
      final BEFile.State state,
      final byte[] data,
      String sessionToken,
      ProgressCallback uploadProgressCallback,
      Task<Void> cancellationToken) {
    if (state.url() != null) { // !isDirty
      return Task.forResult(state);
    }
    if (cancellationToken != null && cancellationToken.isCancelled()) {
      return Task.cancelled();
    }

    final BERESTCommand command = new BERESTFileCommand.Builder()
        .fileName(state.name())
        .data(data)
        .contentType(state.mimeType())
        .sessionToken(sessionToken)
        .build();
    command.enableRetrying();

    return command.executeAsync(
        restClient,
        uploadProgressCallback,
        null,
        cancellationToken
    ).onSuccess(new Continuation<JSONObject, BEFile.State>() {
      @Override
      public BEFile.State then(Task<JSONObject> task) throws Exception {
        JSONObject result = task.getResult();
        BEFile.State newState = new BEFile.State.Builder(state)
            .name(result.getString("name"))
            .url(result.getString("url"))
            .build();

        // Write data to cache
        try {
          BEFileUtils.writeByteArrayToFile(getCacheFile(newState), data);
        } catch (IOException e) {
          // do nothing
        }

        return newState;
      }
    }, BEExecutors.io());
  }

  public Task<BEFile.State> saveAsync(
      final BEFile.State state,
      final File file,
      String sessionToken,
      ProgressCallback uploadProgressCallback,
      Task<Void> cancellationToken) {
    if (state.url() != null) { // !isDirty
      return Task.forResult(state);
    }
    if (cancellationToken != null && cancellationToken.isCancelled()) {
      return Task.cancelled();
    }

    final BERESTCommand command = new BERESTFileCommand.Builder()
        .fileName(state.name())
        .file(file)
        .contentType(state.mimeType())
        .sessionToken(sessionToken)
        .build();
    command.enableRetrying();

    return command.executeAsync(
        restClient,
        uploadProgressCallback,
        null,
        cancellationToken
    ).onSuccess(new Continuation<JSONObject, BEFile.State>() {
      @Override
      public BEFile.State then(Task<JSONObject> task) throws Exception {
        JSONObject result = task.getResult();
        BEFile.State newState = new BEFile.State.Builder(state)
            .name(result.getString("name"))
            .url(result.getString("url"))
            .build();

        // Write data to cache
        try {
          BEFileUtils.copyFile(file, getCacheFile(newState));
        } catch (IOException e) {
          // do nothing
        }

        return newState;
      }
    }, BEExecutors.io());
  }

  public Task<File> fetchAsync(
      final BEFile.State state,
      @SuppressWarnings("UnusedParameters") String sessionToken,
      final ProgressCallback downloadProgressCallback,
      final Task<Void> cancellationToken) {
    if (cancellationToken != null && cancellationToken.isCancelled()) {
      return Task.cancelled();
    }
    final File cacheFile = getCacheFile(state);
    return Task.call(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return cacheFile.exists();
      }
    }, BEExecutors.io()).continueWithTask(new Continuation<Boolean, Task<File>>() {
      @Override
      public Task<File> then(Task<Boolean> task) throws Exception {
        boolean result = task.getResult();
        if (result) {
          return Task.forResult(cacheFile);
        }
        if (cancellationToken != null && cancellationToken.isCancelled()) {
          return Task.cancelled();
        }

        // Generate the temp file path for caching BEFile content based on BEFile's url
        // The reason we do not write to the cacheFile directly is because there is no way we can
        // verify if a cacheFile is complete or not. If download is interrupted in the middle, next
        // time when we download the BEFile, since cacheFile has already existed, we will return
        // this incomplete cacheFile
        final File tempFile = getTempFile(state);

        // network
        final BEAWSRequest request =
            new BEAWSRequest(BEHttpRequest.Method.GET, state.url(), tempFile);

        // We do not need to delete the temp file since we always try to overwrite it
        return request.executeAsync(
            awsClient(),
            null,
            downloadProgressCallback,
            cancellationToken).continueWithTask(new Continuation<Void, Task<File>>() {
          @Override
          public Task<File> then(Task<Void> task) throws Exception {
            // If the top-level task was cancelled, don't actually set the data -- just move on.
            if (cancellationToken != null && cancellationToken.isCancelled()) {
              throw new CancellationException();
            }
            if (task.isFaulted()) {
              BEFileUtils.deleteQuietly(tempFile);
              return task.cast();
            }

            // Since we give the cacheFile pointer to developers, it is not safe to guarantee
            // cacheFile always does not exist here, so it is better to delete it manually,
            // otherwise moveFile may throw an exception.
            BEFileUtils.deleteQuietly(cacheFile);
            BEFileUtils.moveFile(tempFile, cacheFile);
            return Task.forResult(cacheFile);
          }
        }, BEExecutors.io());
      }
    });
  }
}
