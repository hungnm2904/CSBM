package com.csbm;

import java.util.Arrays;
import java.util.Map;

import bolts.Continuation;
import bolts.Task;

/** package */ class CachedCurrentUserController implements BECurrentUserController {

  /**
   * Lock used to synchronize current user modifications and access.
   *
   * Note about lock ordering:
   *
   * You must NOT acquire the BEUser instance mutex (the "mutex" field in BEObject) while
   * holding this static initialization lock. Doing so will cause a deadlock. Here's an example:
   * https://phabricator.fb.com/P17182641
   */
  private final Object mutex = new Object();
  private final TaskQueue taskQueue = new TaskQueue();

  private final BEObjectStore<BEUser> store;

  /* package */ BEUser currentUser;
  // Whether currentUser is known to match the serialized version on disk. This is useful for saving
  // a filesystem check if you try to load currentUser frequently while there is none on disk.
  /* package */ boolean currentUserMatchesDisk = false;

  public CachedCurrentUserController(BEObjectStore<BEUser> store) {
    this.store = store;
  }

  @Override
  public Task<Void> setAsync(final BEUser user) {
    return taskQueue.enqueue(new Continuation<Void, Task<Void>>() {
      @Override
      public Task<Void> then(Task<Void> toAwait) throws Exception {
        return toAwait.continueWithTask(new Continuation<Void, Task<Void>>() {
          @Override
          public Task<Void> then(Task<Void> task) throws Exception {
            BEUser oldCurrentUser;
            synchronized (mutex) {
              oldCurrentUser = currentUser;
            }

            if (oldCurrentUser != null && oldCurrentUser != user) {
              // We don't need to revoke the token since we're not explicitly calling logOut
              // We don't need to remove persisted files since we're overwriting them
              return oldCurrentUser.logOutAsync(false).continueWith(new Continuation<Void, Void>() {
                @Override
                public Void then(Task<Void> task) throws Exception {
                  return null; // ignore errors
                }
              });
            }
            return task;
          }
        }).onSuccessTask(new Continuation<Void, Task<Void>>() {
          @Override
          public Task<Void> then(Task<Void> task) throws Exception {
            user.setIsCurrentUser(true);
            return user.synchronizeAllAuthDataAsync();
          }
        }).onSuccessTask(new Continuation<Void, Task<Void>>() {
          @Override
          public Task<Void> then(Task<Void> task) throws Exception {
            return store.setAsync(user).continueWith(new Continuation<Void, Void>() {
              @Override
              public Void then(Task<Void> task) throws Exception {
                synchronized (mutex) {
                  currentUserMatchesDisk = !task.isFaulted();
                  currentUser = user;
                }
                return null;
              }
            });
          }
        });
      }
    });
  }

  @Override
  public Task<Void> setIfNeededAsync(BEUser user) {
    synchronized (mutex) {
      if (!user.isCurrentUser() || currentUserMatchesDisk) {
        return Task.forResult(null);
      }
    }

    return setAsync(user);
  }

  @Override
  public Task<BEUser> getAsync() {
    return getAsync(BEUser.isAutomaticUserEnabled());
  }

  @Override
  public Task<Boolean> existsAsync() {
    synchronized (mutex) {
      if (currentUser != null) {
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
  public boolean isCurrent(BEUser user) {
    synchronized (mutex) {
      return currentUser == user;
    }
  }

  @Override
  public void clearFromMemory() {
    synchronized (mutex) {
      currentUser = null;
      currentUserMatchesDisk = false;
    }
  }

  @Override
  public void clearFromDisk() {
    synchronized (mutex) {
      currentUser = null;
      currentUserMatchesDisk = false;
    }
    try {
      BETaskUtils.wait(store.deleteAsync());
    } catch (BEException e) {
      // ignored
    }
  }

  @Override
  public Task<String> getCurrentSessionTokenAsync() {
    return getAsync(false).onSuccess(new Continuation<BEUser, String>() {
      @Override
      public String then(Task<BEUser> task) throws Exception {
        BEUser user = task.getResult();
        return user != null ? user.getSessionToken() : null;
      }
    });
  }

  @Override
  public Task<Void> logOutAsync() {
    return taskQueue.enqueue(new Continuation<Void, Task<Void>>() {
      @Override
      public Task<Void> then(Task<Void> toAwait) throws Exception {
        // We can parallelize disk and network work, but only after we restore the current user from
        // disk.
        final Task<BEUser> userTask = getAsync(false);
        return Task.whenAll(Arrays.asList(userTask, toAwait)).continueWithTask(new Continuation<Void, Task<Void>>() {
          @Override
          public Task<Void> then(Task<Void> task) throws Exception {
            Task<Void> logOutTask = userTask.onSuccessTask(new Continuation<BEUser, Task<Void>>() {
              @Override
              public Task<Void> then(Task<BEUser> task) throws Exception {
                BEUser user = task.getResult();
                if (user == null) {
                  return task.cast();
                }
                return user.logOutAsync();
              }
            });

            Task<Void> diskTask = store.deleteAsync().continueWith(new Continuation<Void, Void>() {
              @Override
              public Void then(Task<Void> task) throws Exception {
                boolean deleted = !task.isFaulted();
                synchronized (mutex) {
                  currentUserMatchesDisk = deleted;
                  currentUser = null;
                }
                return null;
              }
            });
            return Task.whenAll(Arrays.asList(logOutTask, diskTask));
          }
        });
      }
    });
  }

  @Override
  public Task<BEUser> getAsync(final boolean shouldAutoCreateUser) {
    synchronized (mutex) {
      if (currentUser != null) {
        return Task.forResult(currentUser);
      }
    }

    return taskQueue.enqueue(new Continuation<Void, Task<BEUser>>() {
      @Override
      public Task<BEUser> then(Task<Void> toAwait) throws Exception {
        return toAwait.continueWithTask(new Continuation<Void, Task<BEUser>>() {
          @Override
          public Task<BEUser> then(Task<Void> ignored) throws Exception {
            BEUser current;
            boolean matchesDisk;
            synchronized (mutex) {
              current = currentUser;
              matchesDisk = currentUserMatchesDisk;
            }

            if (current != null) {
              return Task.forResult(current);
            }

            if (matchesDisk) {
              if (shouldAutoCreateUser) {
                return Task.forResult(lazyLogIn());
              }
              return null;
            }

            return store.getAsync().continueWith(new Continuation<BEUser, BEUser>() {
              @Override
              public BEUser then(Task<BEUser> task) throws Exception {
                BEUser current = task.getResult();
                boolean matchesDisk = !task.isFaulted();

                synchronized (mutex) {
                  currentUser = current;
                  currentUserMatchesDisk = matchesDisk;
                }

                if (current != null) {
                  synchronized (current.mutex) {
                    current.setIsCurrentUser(true);
                  }
                  return current;
                }

                if (shouldAutoCreateUser) {
                  return lazyLogIn();
                }
                return null;
              }
            });
          }
        });
      }
    });
  }

  private BEUser lazyLogIn() {
    Map<String, String> authData = BEAnonymousUtils.getAuthData();
    return lazyLogIn(BEAnonymousUtils.AUTH_TYPE, authData);
  }

  /* package for tests */ BEUser lazyLogIn(String authType, Map<String, String> authData) {
    // Note: if authType != BEAnonymousUtils.AUTH_TYPE the user is not "lazy".
    BEUser user = BEObject.create(BEUser.class);
    synchronized (user.mutex) {
      user.setIsCurrentUser(true);
      user.putAuthData(authType, authData);
    }

    synchronized (mutex) {
      currentUserMatchesDisk = false;
      currentUser = user;
    }

    return user;
  }
}
