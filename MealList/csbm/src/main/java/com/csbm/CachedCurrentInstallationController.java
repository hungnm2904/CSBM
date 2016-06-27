package com.csbm;

import bolts.Continuation;
import bolts.Task;

/** package */ class CachedCurrentInstallationController
    implements BECurrentInstallationController {

  /* package */ static final String TAG = "com.csbm.CachedCurrentInstallationController";

  /*
   * Note about lock ordering:
   *
   * You must NOT acquire the BEInstallation instance mutex (the "mutex" field in BEObject)
   * while holding this current installation lock. (We used to use the BEInstallation.class lock,
   * but moved on to an explicit lock object since anyone could acquire the BEInstallation.class
   * lock as BEInstallation is a public class.) Acquiring the instance mutex while holding this
   * current installation lock will lead to a deadlock. Here is an example:
   * https://phabricator.fb.com/P3251091
   */
  private final Object mutex = new Object();

  private final TaskQueue taskQueue = new TaskQueue();

  private final BEObjectStore<BEInstallation> store;
  private final InstallationId installationId;

  // The "current installation" is the installation for this device. Protected by
  // mutex.
  /* package for test */ BEInstallation currentInstallation;

  public CachedCurrentInstallationController(
      BEObjectStore<BEInstallation> store, InstallationId installationId) {
    this.store = store;
    this.installationId = installationId;
  }

  @Override
  public Task<Void> setAsync(final BEInstallation installation) {
    if (!isCurrent(installation)) {
      return Task.forResult(null);
    }

    return taskQueue.enqueue(new Continuation<Void, Task<Void>>() {
      @Override
      public Task<Void> then(Task<Void> toAwait) throws Exception {
        return toAwait.continueWithTask(new Continuation<Void, Task<Void>>() {
          @Override
          public Task<Void> then(Task<Void> task) throws Exception {
            return store.setAsync(installation);
          }
        }).continueWithTask(new Continuation<Void, Task<Void>>() {
          @Override
          public Task<Void> then(Task<Void> task) throws Exception {
            installationId.set(installation.getInstallationId());
            return task;
          }
        }, BEExecutors.io());
      }
    });
  }

  @Override
  public Task<BEInstallation> getAsync() {
    synchronized (mutex) {
      if (currentInstallation != null) {
        return Task.forResult(currentInstallation);
      }
    }

    return taskQueue.enqueue(new Continuation<Void, Task<BEInstallation>>() {
      @Override
      public Task<BEInstallation> then(Task<Void> toAwait) throws Exception {
        return toAwait.continueWithTask(new Continuation<Void, Task<BEInstallation>>() {
          @Override
          public Task<BEInstallation> then(Task<Void> task) throws Exception {
            synchronized (mutex) {
              if (currentInstallation != null) {
                return Task.forResult(currentInstallation);
              }
            }

            return store.getAsync().continueWith(new Continuation<BEInstallation, BEInstallation>() {
              @Override
              public BEInstallation then(Task<BEInstallation> task) throws Exception {
                BEInstallation current = task.getResult();
                if (current == null) {
                  current = BEObject.create(BEInstallation.class);
                  current.updateDeviceInfo(installationId);
                } else {
                  installationId.set(current.getInstallationId());
                  PLog.v(TAG, "Successfully deserialized Installation object");
                }

                synchronized (mutex) {
                  currentInstallation = current;
                }
                return current;
              }
            }, BEExecutors.io());
          }
        });
      }
    });
  }

  @Override
  public Task<Boolean> existsAsync() {
    synchronized (mutex) {
      if (currentInstallation != null) {
        return Task.forResult(true);
      }
    }

    return taskQueue.enqueue(new Continuation<Void, Task<Boolean>>() {
      @Override
      public Task<Boolean> then(Task<Void> toAwait) throws Exception {
        return toAwait.continueWithTask(new Continuation<Void, Task<Boolean>>() {
          @Override
          public Task<Boolean> then(Task<Void> task) throws Exception {
            return store.existsAsync();
          }
        });
      }
    });
  }

  @Override
  public void clearFromMemory() {
    synchronized (mutex) {
      currentInstallation = null;
    }
  }

  @Override
  public void clearFromDisk() {
    synchronized (mutex) {
      currentInstallation = null;
    }
    try {
      installationId.clear();
      BETaskUtils.wait(store.deleteAsync());
    } catch (BEException e) {
      // ignored
    }
  }

  @Override
  public boolean isCurrent(BEInstallation installation) {
    synchronized (mutex) {
      return  currentInstallation == installation;
    }
  }
}
