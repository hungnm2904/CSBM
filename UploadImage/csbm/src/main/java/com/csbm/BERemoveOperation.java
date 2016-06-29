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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * An operation that removes every instance of an element from an array field.
 */

/** package */ class BERemoveOperation implements BEFieldOperation {
  protected final HashSet<Object> objects = new HashSet<>();

  public BERemoveOperation(Collection<?> coll) {
    objects.addAll(coll);
  }

  @Override
  public JSONObject encode(BEEncoder objectEncoder) throws JSONException {
    JSONObject output = new JSONObject();
    output.put("__op", "Remove");
    output.put("objects", objectEncoder.encode(new ArrayList<>(objects)));
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
      if (value instanceof JSONArray || value instanceof List) {
        return new BESetOperation(this.apply(value, null));
      } else {
        throw new IllegalArgumentException("You can only add an item to a List or JSONArray.");
      }
    } else if (previous instanceof BERemoveOperation) {
      HashSet<Object> result = new HashSet<>(((BERemoveOperation) previous).objects);
      result.addAll(objects);
      return new BERemoveOperation(result);
    } else {
      throw new IllegalArgumentException("Operation is invalid after previous operation.");
    }
  }

  @Override
  public Object apply(Object oldValue, String key) {
    if (oldValue == null) {
      return new ArrayList<>();
    } else if (oldValue instanceof JSONArray) {
      ArrayList<Object> old = BEFieldOperations.jsonArrayAsArrayList((JSONArray) oldValue);
      @SuppressWarnings("unchecked")
      ArrayList<Object> newValue = (ArrayList<Object>) this.apply(old, key);
      return new JSONArray(newValue);
    } else if (oldValue instanceof List) {
      ArrayList<Object> result = new ArrayList<>((List<?>) oldValue);
      result.removeAll(objects);

      // Remove the removed objects from "objects" -- the items remaining
      // should be ones that weren't removed by object equality.
      ArrayList<Object> objectsToBeRemoved = new ArrayList<>(objects);
      objectsToBeRemoved.removeAll(result);

      // Build up set of object IDs for any BEObjects in the remaining objects-to-be-removed
      HashSet<String> objectIds = new HashSet<>();
      for (Object obj : objectsToBeRemoved) {
        if (obj instanceof BEObject) {
          objectIds.add(((BEObject) obj).getObjectId());
        }
      }

      // And iterate over "result" to see if any other BEObjects need to be removed
      Iterator<Object> resultIterator = result.iterator();
      while (resultIterator.hasNext()) {
        Object obj = resultIterator.next();
        if (obj instanceof BEObject && objectIds.contains(((BEObject) obj).getObjectId())) {
          resultIterator.remove();
        }
      }
      return result;
    } else {
      throw new IllegalArgumentException("Operation is invalid after previous operation.");
    }
  }
}
