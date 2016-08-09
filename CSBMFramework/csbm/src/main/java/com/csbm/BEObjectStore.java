package com.csbm;

import bolts.Task;

/** package */ interface BEObjectStore<T extends BEObject> {

  Task<T> getAsync();

  Task<Void> setAsync(T object);

  Task<Boolean> existsAsync();

  Task<Void> deleteAsync();
}
