/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
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
