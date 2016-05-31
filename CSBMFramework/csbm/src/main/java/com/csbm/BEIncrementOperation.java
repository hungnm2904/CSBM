/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.csbm;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An operation that increases a numeric field's value by a given amount.
 */

/** package */ class BEIncrementOperation implements BEFieldOperation {
  private final Number amount;

  public BEIncrementOperation(Number amount) {
    this.amount = amount;
  }

  @Override
  public JSONObject encode(BEEncoder objectEncoder) throws JSONException {
    JSONObject output = new JSONObject();
    output.put("__op", "Increment");
    output.put("amount", amount);
    return output;
  }

  @Override
  public BEFieldOperation mergeWithPrevious(BEFieldOperation previous) {
    if (previous == null) {
      return this;
    } else if (previous instanceof BEDeleteOperation) {
      return new BESetOperation(amount);
    } else if (previous instanceof BESetOperation) {
      Object oldValue = ((BESetOperation) previous).getValue();
      if (oldValue instanceof Number) {
        return new BESetOperation(Numbers.add((Number) oldValue, amount));
      } else {
        throw new IllegalArgumentException("You cannot increment a non-number.");
      }
    } else if (previous instanceof BEIncrementOperation) {
      Number oldAmount = ((BEIncrementOperation) previous).amount;
      return new BEIncrementOperation(Numbers.add(oldAmount, amount));
    } else {
      throw new IllegalArgumentException("Operation is invalid after previous operation.");
    }
  }

  @Override
  public Object apply(Object oldValue, String key) {
    if (oldValue == null) {
      return amount;
    } else if (oldValue instanceof Number) {
      return Numbers.add((Number) oldValue, amount);
    } else {
      throw new IllegalArgumentException("You cannot increment a non-number.");
    }
  }
}
