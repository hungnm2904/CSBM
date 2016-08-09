package com.csbm;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

import bolts.Task;

/** package */ class BEExecutors {

  private static ScheduledExecutorService scheduledExecutor;
  private static final Object SCHEDULED_EXECUTOR_LOCK = new Object();
  /**
   * Long running operations should NOT be put onto SCHEDULED_EXECUTOR.
   */
  /* package */ static ScheduledExecutorService scheduled() {
    synchronized (SCHEDULED_EXECUTOR_LOCK) {
      if (scheduledExecutor == null) {
        scheduledExecutor = java.util.concurrent.Executors.newScheduledThreadPool(1);
      }
    }
    return scheduledExecutor;
  }

  /* package */ static Executor main() {
    return Task.UI_THREAD_EXECUTOR;
  }

  /* package */ static Executor io() {
    return Task.BACKGROUND_EXECUTOR;
  }
}
