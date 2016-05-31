/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.csbm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An operation that adds a new element to an array field.
 */

/** package */ class BEAddOperation implements BEFieldOperation {
  protected final ArrayList<Object> objects = new ArrayList<>();

  public BEAddOperation(Collection<?> coll) {
    objects.addAll(coll);
  }

  @Override
  public JSONObject encode(BEEncoder objectEncoder) throws JSONException {
    JSONObject output = new JSONObject();
    output.put("__op", "Add");
    output.put("objects", objectEncoder.encode(objects));
    return output;
  }

  @Override
  public BEFieldOperation mergeWithPrevious(BEFieldOperation previous) {
    if (previous == null) {
      return this;
    } else if (previous instanceof BEDeleteOperation) {
      return new BESetOperation(objects);
    } else if (previous instanceof BESetOperation) {
      Object value = ((BESetOperation) previous).getValue();
      if (value instanceof JSONArray) {
        ArrayList<Object> result = BEFieldOperations.jsonArrayAsArrayList((JSONArray) value);
        result.addAll(objects);
        return new BESetOperation(new JSONArray(result));
      } else if (value instanceof List) {
        ArrayList<Object> result = new ArrayList<>((List<?>) value);
        result.addAll(objects);
        return new BESetOperation(result);
      } else {
        throw new IllegalArgumentException("You can only add an item to a List or JSONArray.");
      }
    } else if (previous instanceof BEAddOperation) {
      ArrayList<Object> result = new ArrayList<>(((BEAddOperation) previous).objects);
      result.addAll(objects);
      return new BEAddOperation(result);
    } else {
      throw new IllegalArgumentException("Operation is invalid after previous operation.");
    }
  }

  @Override
  public Object apply(Object oldValue, String key) {
    if (oldValue == null) {
      return objects;
    } else if (oldValue instanceof JSONArray) {
      ArrayList<Object> old = BEFieldOperations.jsonArrayAsArrayList((JSONArray) oldValue);
      @SuppressWarnings("unchecked")
      ArrayList<Object> newValue = (ArrayList<Object>) this.apply(old, key);
      return new JSONArray(newValue);
    } else if (oldValue instanceof List) {
      ArrayList<Object> result = new ArrayList<>((List<?>) oldValue);
      result.addAll(objects);
      return result;
    } else {
      throw new IllegalArgumentException("Operation is invalid after previous operation.");
    }
  }
}
