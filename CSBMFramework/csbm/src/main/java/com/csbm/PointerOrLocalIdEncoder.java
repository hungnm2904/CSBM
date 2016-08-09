package com.csbm;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Encodes {@link BEObjects} as pointers. If the object does not have an objectId, uses a
 * local id.
 */

/** package */ class PointerOrLocalIdEncoder extends BEEncoder {

  // This class isn't really a Singleton, but since it has no state, it's more efficient to get the
  // default instance.
  private static final PointerOrLocalIdEncoder INSTANCE = new PointerOrLocalIdEncoder();
  public static PointerOrLocalIdEncoder get() {
    return INSTANCE;
  }

  @Override
  public JSONObject encodeRelatedObject(BEObject object) {
    JSONObject json = new JSONObject();
    try {
      if (object.getObjectId() != null) {
        json.put("__type", "Pointer");
        json.put("className", object.getClassName());
        json.put("objectId", object.getObjectId());
      } else {
        json.put("__type", "Pointer");
        json.put("className", object.getClassName());
        json.put("localId", object.getOrCreateLocalId());
      }
    } catch (JSONException e) {
      // This should not happen
      throw new RuntimeException(e);
    }
    return json;
  }
}
