package com.csbm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/** package */ class BEMulticastDelegate<T> {
  private final List<BECallback2<T, BEException>> callbacks;

  public BEMulticastDelegate() {
    callbacks = new LinkedList<>();
  }

  public void subscribe(BECallback2<T, BEException> callback) {
    callbacks.add(callback);
  }

  public void unsubscribe(BECallback2<T, BEException> callback) {
    callbacks.remove(callback);
  }

  public void invoke(T result, BEException exception) {
    for (BECallback2<T, BEException> callback : new ArrayList<>(callbacks)) {
      callback.done(result, exception);
    }
  }

  public void clear() {
    callbacks.clear();
  }
}
