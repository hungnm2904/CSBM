/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.csbm;

/**
 * An operation where a field is set to a given value regardless of its previous value.
 */
/** package */ class BESetOperation implements BEFieldOperation {
  private final Object value;

  public BESetOperation(Object newValue) {
    value = newValue;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public Object encode(BEEncoder objectEncoder) {
    return objectEncoder.encode(value);
  }

  @Override
  public BEFieldOperation mergeWithPrevious(BEFieldOperation previous) {
    return this;
  }

  @Override
  public Object apply(Object oldValue, String key) {
    return value;
  }
}
