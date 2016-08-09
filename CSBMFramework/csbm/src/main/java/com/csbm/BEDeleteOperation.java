package com.csbm;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An operation where a field is deleted from the object.
 */

/** package */ class BEDeleteOperation implements BEFieldOperation {
  private static final BEDeleteOperation defaultInstance = new BEDeleteOperation();

  public static BEDeleteOperation getInstance() {
    return defaultInstance;
  }

  private BEDeleteOperation() {
  }

  @Override
  public JSONObject encode(BEEncoder objectEncoder) throws JSONException {
    JSONObject output = new JSONObject();
    output.put("__op", "Delete");
    return output;
  }

  @Override
  public BEFieldOperation mergeWithPrevious(BEFieldOperation previous) {
    return this;
  }

  @Override
  public Object apply(Object oldValue, String key) {
    return null;
  }
}
