package com.csbm;

import org.json.JSONObject;

/**
 * Throws an exception if someone attemps to encode a {@code BEObject}.
 */

/** package */ class NoObjectsEncoder extends BEEncoder {

  // This class isn't really a Singleton, but since it has no state, it's more efficient to get the
  // default instance.
  private static final NoObjectsEncoder INSTANCE = new NoObjectsEncoder();
  public static NoObjectsEncoder get() {
    return INSTANCE;
  }

  @Override
  public JSONObject encodeRelatedObject(BEObject object) {
    throw new IllegalArgumentException("BEObjects not allowed here");
  }
}
