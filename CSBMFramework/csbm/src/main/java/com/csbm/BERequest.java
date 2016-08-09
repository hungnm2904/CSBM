package com.csbm;

import android.os.Build;

import com.csbm.http.BEHttpBody;
import com.csbm.http.BEHttpRequest;
import com.csbm.http.BEHttpResponse;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

/**
 * BERequest takes an arbitrary HttpUriRequest and retries it a number of times with
 * exponential backoff.
 */

/** package */ abstract class BERequest<Response> {

  private static final ThreadFactory sThreadFactory = new ThreadFactory() {
    private final AtomicInteger mCount = new AtomicInteger(1);

    public Thread newThread(Runnable r) {
      return new Thread(r, "BERequest.NETWORK_EXECUTOR-thread-" + mCount.getAndIncrement());
    }
  };

  /**
   * We want to use more threads than default in {@code bolts.Executors} since most of the time
   * the threads will be asleep waiting for data.
   */
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final int CORE_POOL_SIZE = CPU_COUNT * 2 + 1;
  private static final int MAX_POOL_SIZE = CPU_COUNT * 2 * 2 + 1;
  private static final long KEEP_ALIVE_TIME = 1L;
  private static final int MAX_QUEUE_SIZE = 128;

  private static ThreadPoolExecutor newThreadPoolExecutor(int corePoolSize, int maxPoolSize,
                                                          long keepAliveTime, TimeUnit timeUnit, BlockingQueue<Runnable> workQueue,
                                                          ThreadFactory threadFactory) {
    ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize,
        keepAliveTime, timeUnit, workQueue, threadFactory);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
      executor.allowCoreThreadTimeOut(true);
    }
    return executor;
  }

  /* package */ static final ExecutorService NETWORK_EXECUTOR = newThreadPoolExecutor(
      CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
      new LinkedBlockingQueue<Runnable>(MAX_QUEUE_SIZE), sThreadFactory);

  protected static final int DEFAULT_MAX_RETRIES = 4;
  /* package */ static final long DEFAULT_INITIAL_RETRY_DELAY = 1000L;

  private static long defaultInitialRetryDelay = DEFAULT_INITIAL_RETRY_DELAY;

  public static void setDefaultInitialRetryDelay(long delay) {
    defaultInitialRetryDelay = delay;
  }
  public static long getDefaultInitialRetryDelay() {
    return defaultInitialRetryDelay;
  }

  private int maxRetries = DEFAULT_MAX_RETRIES;

  /* package */ BEHttpRequest.Method method;
  /* package */ String url;

  public BERequest(String url) {
    this(BEHttpRequest.Method.GET, url);
  }

  public BERequest(BEHttpRequest.Method method, String url) {
    this.method = method;
    this.url = url;
  }

  public void setMaxRetries(int max) {
    maxRetries = max;
  }

  protected BEHttpBody newBody(ProgressCallback uploadProgressCallback) {
    // do nothing
    return null;
  }

  protected BEHttpRequest newRequest(
      BEHttpRequest.Method method,
      String url,
      ProgressCallback uploadProgressCallback)  {
    BEHttpRequest.Builder requestBuilder = new BEHttpRequest.Builder()
        .setMethod(method)
        .setUrl(url);

    switch (method) {
      case GET:
      case DELETE:
        break;
      case POST:
      case PUT:
        requestBuilder.setBody(newBody(uploadProgressCallback));
        break;
      default:
        throw new IllegalStateException("Invalid method " + method);
    }
    return requestBuilder.build();
  }

  /*
   * Runs one iteration of the request.
   */
  private Task<Response> sendOneRequestAsync(
      final BEHttpClient client,
      final BEHttpRequest request,
      final ProgressCallback downloadProgressCallback) {
    return Task.<Void>forResult(null).onSuccessTask(new Continuation<Void, Task<Response>>() {
      @Override
      public Task<Response> then(Task<Void> task) throws Exception {
        BEHttpResponse response = client.execute(request);
        return onResponseAsync(response, downloadProgressCallback);
      }
    }, NETWORK_EXECUTOR).continueWithTask(new Continuation<Response, Task<Response>>() {
      @Override
      public Task<Response> then(Task<Response> task) throws Exception {
        if (task.isFaulted()) {
          Exception error = task.getError();
          if (error instanceof IOException) {
            return Task.forError(newTemporaryException("i/o failure", error));
          }
        }
        return task;
      }
    }, Task.BACKGROUND_EXECUTOR);
  }

  protected abstract Task<Response> onResponseAsync(BEHttpResponse response,
      ProgressCallback downloadProgressCallback);

  /*
   * Starts retrying the block.
   */
  public Task<Response> executeAsync(final BEHttpClient client) {
    return executeAsync(client, (ProgressCallback) null, null, null);
  }

  public Task<Response> executeAsync(final BEHttpClient client, Task<Void> cancellationToken) {
    return executeAsync(client, (ProgressCallback) null, null, cancellationToken);
  }

  public Task<Response> executeAsync(
      final BEHttpClient client,
      final ProgressCallback uploadProgressCallback,
      final ProgressCallback downloadProgressCallback) {
    return executeAsync(client, uploadProgressCallback, downloadProgressCallback, null);
  }

  public Task<Response> executeAsync(
      final BEHttpClient client,
      final ProgressCallback uploadProgressCallback,
      final ProgressCallback downloadProgressCallback,
      Task<Void> cancellationToken) {
    BEHttpRequest request = newRequest(method, url, uploadProgressCallback);
    return executeAsync(
        client,
        request,
        downloadProgressCallback,
        cancellationToken);
  }

  // Although we can not cancel a single request, but we allow cancel between retries so we need a
  // cancellationToken here.
  private Task<Response> executeAsync(
      final BEHttpClient client,
      final BEHttpRequest request,
      final ProgressCallback downloadProgressCallback,
      final Task<Void> cancellationToken) {
    long delay = defaultInitialRetryDelay + (long) (defaultInitialRetryDelay * Math.random());

    return executeAsync(
        client,
        request,
        0,
        delay,
        downloadProgressCallback,
        cancellationToken);
  }

  private Task<Response> executeAsync(
      final BEHttpClient client,
      final BEHttpRequest request,
      final int attemptsMade,
      final long delay,
      final ProgressCallback downloadProgressCallback,
      final Task<Void> cancellationToken) {
    if (cancellationToken != null && cancellationToken.isCancelled()) {
      return Task.cancelled();
    }
    return sendOneRequestAsync(client, request, downloadProgressCallback).continueWithTask(new Continuation<Response, Task<Response>>() {
      @Override
      public Task<Response> then(Task<Response> task) throws Exception {
        Exception e = task.getError();
        if (task.isFaulted() && e instanceof BEException) {
          if (cancellationToken != null && cancellationToken.isCancelled()) {
            return Task.cancelled();
          }

          if (e instanceof BERequestException &&
              ((BERequestException) e).isPermanentFailure) {
            return task;
          }

          if (attemptsMade < maxRetries) {
            PLog.i("com.csbm.BERequest", "Request failed. Waiting " + delay
                + " milliseconds before attempt #" + (attemptsMade + 1));

            final TaskCompletionSource<Response> retryTask = new TaskCompletionSource<>();
            BEExecutors.scheduled().schedule(new Runnable() {
              @Override
              public void run() {
                executeAsync(
                    client,
                    request,
                    attemptsMade + 1,
                    delay * 2,
                    downloadProgressCallback,
                    cancellationToken).continueWithTask(new Continuation<Response, Task<Void>>() {
                  @Override
                  public Task<Void> then(Task<Response> task) throws Exception {
                    if (task.isCancelled()) {
                      retryTask.setCancelled();
                    } else if (task.isFaulted()) {
                      retryTask.setError(task.getError());
                    } else {
                      retryTask.setResult(task.getResult());
                    }
                    return null;
                  }
                });
              }
            }, delay, TimeUnit.MILLISECONDS);
            return retryTask.getTask();
          }
        }
        return task;
      }
    });
  }

  /**
   * Constructs a permanent exception that won't be retried.
   */
  protected BEException newPermanentException(int code, String message) {
    BERequestException e = new BERequestException(code, message);
    e.isPermanentFailure = true;
    return e;
  }

  /**
   * Constructs a temporary exception that will be retried.
   */
  protected BEException newTemporaryException(int code, String message) {
    BERequestException e = new BERequestException(code, message);
    e.isPermanentFailure = false;
    return e;
  }

  /**
   * Constructs a temporary exception that will be retried with json error code 100.
   *
   * @see BEException#CONNECTION_FAILED
   */
  protected BEException newTemporaryException(String message, Throwable t) {
    BERequestException e = new BERequestException(
        BEException.CONNECTION_FAILED, message, t);
    e.isPermanentFailure = false;
    return e;
  }

  private static class BERequestException extends BEException {
    boolean isPermanentFailure = false;

    public BERequestException(int theCode, String theMessage) {
      super(theCode, theMessage);
    }

    public BERequestException(int theCode, String message, Throwable cause) {
      super(theCode, message, cause);
    }
  }
}
