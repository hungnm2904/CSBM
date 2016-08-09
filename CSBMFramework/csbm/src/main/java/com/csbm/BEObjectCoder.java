package com.csbm;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Handles encoding/decoding BEObjects to/from REST JSON.
 */

/** package */ class BEObjectCoder {

  private static final String KEY_OBJECT_ID = "objectId";
  private static final String KEY_CLASS_NAME = "className";
  private static final String KEY_ACL = "ACL";
  private static final String KEY_CREATED_AT = "createdAt";
  private static final String KEY_UPDATED_AT = "updatedAt";

  private static final BEObjectCoder INSTANCE = new BEObjectCoder();
  public static BEObjectCoder get() {
    return INSTANCE;
  }

  /* package */ BEObjectCoder() {
    // do nothing
  }

  /**
   * Converts a {@code BEObject.State} to REST JSON for saving.
   *
   * Only dirty keys from {@code operations} are represented in the data. Non-dirty keys such as
   * {@code updatedAt}, {@code createdAt}, etc. are not included.
   *
   * @param state
   *          {@link BEObject.State} of the type of {@link BEObject} that will be returned.
   *          Properties are completely ignored.
   * @param operations
   *          Dirty operations that are to be saved.
   * @param encoder
   *          Encoder instance that will be used to encode the request.
   * @return
   *          A REST formatted {@link JSONObject} that will be used for saving.
   */
  public <T extends BEObject.State> JSONObject encode(
          T state, BEOperationSet operations, BEEncoder encoder) {
    JSONObject objectJSON = new JSONObject();

    try {
      // Serialize the data
      for (String key : operations.keySet()) {
        BEFieldOperation operation = operations.get(key);
        objectJSON.put(key, encoder.encode(operation));

        // TODO(grantland): Use cached value from hashedObjects if it's a set operation.
      }

      if (state.objectId() != null) {
        objectJSON.put(KEY_OBJECT_ID, state.objectId());
      }
    } catch (JSONException e) {
      throw new RuntimeException("could not serialize object to JSON");
    }

    return objectJSON;
  }

  /**
   * Converts REST JSON response to {@link BEObject.State.Init}.
   *
   * This returns Builder instead of a State since we'll probably want to set some additional
   * properties on it after decoding such as {@link BEObject.State.Init#isComplete()}, etc.
   *
   * @param builder
   *          A {@link BEObject.State.Init} instance that will have the server JSON applied
   *          (mutated) to it. This will generally be a instance created by clearing a mutable
   *          copy of a {@link BEObject.State} to ensure it's an instance of the correct
   *          subclass: {@code state.newBuilder().clear()}
   * @param json
   *          JSON response in REST format from the server.
   * @param decoder
   *          Decoder instance that will be used to decode the server response.
   * @return
   *          The same Builder instance passed in after the JSON is applied.
   */
  public <T extends BEObject.State.Init<?>> T decode(
          T builder, JSONObject json, BEDecoder decoder) {
    try {
      Iterator<?> keys = json.keys();
      while (keys.hasNext()) {
        String key = (String) keys.next();
        /*
        __type:       Returned by queries and cloud functions to designate body is a BEObject
        __className:  Used by fromJSON, should be stripped out by the time it gets here...
         */
        if (key.equals("__type") || key.equals(KEY_CLASS_NAME)) {
          continue;
        }
        if (key.equals(KEY_OBJECT_ID)) {
          String newObjectId = json.getString(key);
          builder.objectId(newObjectId);
          continue;
        }
        if (key.equals(KEY_CREATED_AT)) {
          builder.createdAt(BEDateFormat.getInstance().parse(json.getString(key)));
          continue;
        }
        if (key.equals(KEY_UPDATED_AT)) {
          builder.updatedAt(BEDateFormat.getInstance().parse(json.getString(key)));
          continue;
        }
        if (key.equals(KEY_ACL)) {
          BEACL acl = BEACL.createACLFromJSONObject(json.getJSONObject(key), decoder);
          builder.put(KEY_ACL, acl);
          continue;
        }

        Object value = json.get(key);
        Object decodedObject = decoder.decode(value);
        builder.put(key, decodedObject);
      }

      return builder;
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }
}
