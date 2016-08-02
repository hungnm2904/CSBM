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

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

/**
 * A set of field-level operations that can be performed on an object, corresponding to one command.
 * For example, all of the data for a single call to save() will be packaged here. It is assumed
 * that the BEObject that owns the operations handles thread-safety.
 */

/** package */ class BEOperationSet extends HashMap<String, BEFieldOperation> {
  private static final long serialVersionUID = 1L;

  private static final String REST_KEY_IS_SAVE_EVENTUALLY = "__isSaveEventually";
  private static final String REST_KEY_UUID = "__uuid";

  // A unique id for this operation set.
  private final String uuid;

  // Does this set correspond to a call to saveEventually?
  private boolean isSaveEventually = false;

  /**
   * Creates a new operation set with a random UUID.
   */
  public BEOperationSet() {
    this(UUID.randomUUID().toString());
  }

  public BEOperationSet(BEOperationSet operations) {
    super(operations);
    uuid = operations.getUUID();
    isSaveEventually = operations.isSaveEventually;
  }

  /**
   * Creates a new operation set with the given UUID.
   */
  private BEOperationSet(String uuid) {
    this.uuid = uuid;
  }

  public String getUUID() {
    return uuid;
  }

  public void setIsSaveEventually(boolean value) {
    isSaveEventually = value;
  }

  public boolean isSaveEventually() {
    return isSaveEventually;
  }
  
  /**
   * Merges the changes from the given operation set into this one. Most typically, this is what
   * happens when a save fails and changes need to be rolled into the next save.
   */
  public void mergeFrom(BEOperationSet other) {
    for (String key : other.keySet()) {
      BEFieldOperation operation1 = other.get(key);
      BEFieldOperation operation2 = get(key);
      if (operation2 != null) {
        operation2 = operation2.mergeWithPrevious(operation1);
      } else {
        operation2 = operation1;
      }
      put(key, operation2);
    }
  }

  /**
   * Converts this operation set into its REST format for serializing to LDS.
   */
  public JSONObject toRest(BEEncoder objectEncoder) throws JSONException {
    JSONObject operationSetJSON = new JSONObject();
    for (String key : keySet()) {
      BEFieldOperation op = get(key);
      operationSetJSON.put(key, op.encode(objectEncoder));
    }

    operationSetJSON.put(REST_KEY_UUID, uuid);
    if (isSaveEventually) {
      operationSetJSON.put(REST_KEY_IS_SAVE_EVENTUALLY, true);
    }
    return operationSetJSON;
  }
  
  /**
   * The inverse of toRest. Creates a new OperationSet from the given JSON.
   */
  public static BEOperationSet fromRest(JSONObject json, BEDecoder decoder)
      throws JSONException {
    // Copy the json object to avoid making changes to the old object
    Iterator<String> keysIter = json.keys();
    String[] keys = new String[json.length()];
    int index = 0;
    while (keysIter.hasNext()) {
      String key = keysIter.next();
      keys[index++] = key;
    }

    JSONObject jsonCopy = new JSONObject(json, keys);
    String uuid = (String) jsonCopy.remove(REST_KEY_UUID);
    BEOperationSet operationSet =
        (uuid == null ? new BEOperationSet() : new BEOperationSet(uuid));

    boolean isSaveEventually = jsonCopy.optBoolean(REST_KEY_IS_SAVE_EVENTUALLY);
    jsonCopy.remove(REST_KEY_IS_SAVE_EVENTUALLY);
    operationSet.setIsSaveEventually(isSaveEventually);

    Iterator<?> opKeys = jsonCopy.keys();
    while (opKeys.hasNext()) {
      String opKey = (String) opKeys.next();
      Object value = decoder.decode(jsonCopy.get(opKey));
      BEFieldOperation fieldOp;
      if (opKey.equals("ACL")) {
        value = BEACL.createACLFromJSONObject(jsonCopy.getJSONObject(opKey), decoder);
      }
      if (value instanceof BEFieldOperation) {
        fieldOp = (BEFieldOperation) value;
      } else {
        fieldOp = new BESetOperation(value);
      }
      operationSet.put(opKey, fieldOp);
    }

    return operationSet;
  }
}
