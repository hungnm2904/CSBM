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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * An operation that adds a new element to an array field, only if it wasn't already present.
 */

/** package */ class BEAddUniqueOperation implements BEFieldOperation {
  protected final LinkedHashSet<Object> objects = new LinkedHashSet<>();

  public BEAddUniqueOperation(Collection<?> col) {
    objects.addAll(col);
  }

  @Override
  public JSONObject encode(BEEncoder objectEncoder) throws JSONException {
    JSONObject output = new JSONObject();
    output.put("__op", "AddUnique");
    output.put("objects", objectEncoder.encode(new ArrayList<>(objects)));
    return output;
  }

  @Override
  @SuppressWarnings("unchecked")
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
    } else if (previous instanceof BEAddUniqueOperation) {
      List<Object> previousResult =
          new ArrayList<>(((BEAddUniqueOperation) previous).objects);
      return new BEAddUniqueOperation((List<Object>) this.apply(previousResult, null));
    } else {
      throw new IllegalArgumentException("Operation is invalid after previous operation.");
    }
  }

  @Override
  public Object apply(Object oldValue, String key) {
    if (oldValue == null) {
      return new ArrayList<>(objects);
    } else if (oldValue instanceof JSONArray) {
      ArrayList<Object> old = BEFieldOperations.jsonArrayAsArrayList((JSONArray) oldValue);
      @SuppressWarnings("unchecked")
      ArrayList<Object> newValue = (ArrayList<Object>) this.apply(old, key);
      return new JSONArray(newValue);
    } else if (oldValue instanceof List) {
      ArrayList<Object> result = new ArrayList<>((List<?>) oldValue);

      // Build up a Map of objectIds of the existing BEObjects in this field.
      HashMap<String, Integer> existingObjectIds = new HashMap<>();
      for (int i = 0; i < result.size(); i++) {
        if (result.get(i) instanceof BEObject) {
          existingObjectIds.put(((BEObject) result.get(i)).getObjectId(), i);
        }
      }

      // Iterate over the objects to add. If it already exists in the field,
      // remove the old one and add the new one. Otherwise, just add normally.
      for (Object obj : objects) {
        if (obj instanceof BEObject) {
          String objectId = ((BEObject) obj).getObjectId();
          if (objectId != null && existingObjectIds.containsKey(objectId)) {
            result.set(existingObjectIds.get(objectId), obj);
          } else if (!result.contains(obj)) {
            result.add(obj);
          }
        } else {
          if (!result.contains(obj)) {
            result.add(obj);
          }
        }
      }
      return result;
    } else {
      throw new IllegalArgumentException("Operation is invalid after previous operation.");
    }
  }
}
