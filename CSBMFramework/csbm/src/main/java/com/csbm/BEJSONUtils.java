package com.csbm;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Static utility methods pertaining to {@link JSONObject} and {@link JSONArray} instances.
 */

/** package */ class BEJSONUtils {

  /**
   * Creates a copy of {@code copyFrom}, excluding the keys from {@code excludes}.
   */
  public static JSONObject create(JSONObject copyFrom, Collection<String> excludes) {
    JSONObject json = new JSONObject();
    Iterator<String> iterator = copyFrom.keys();
    while (iterator.hasNext()) {
      String name = iterator.next();
      if (excludes.contains(name)) {
        continue;
      }
      try {
        json.put(name, copyFrom.opt(name));
      } catch (JSONException e) {
        // This shouldn't ever happen since it'll only throw if `name` is null
        throw new RuntimeException(e);
      }
    }
    return json;
  }

  /**
   * A helper for nonugly iterating over JSONObject keys.
   */
  public static Iterable<String> keys(JSONObject object) {
    final JSONObject finalObject = object;
    return new Iterable<String>() {
      @Override
      public Iterator<String> iterator() {
        return finalObject.keys();
      }
    };
  }

  /**
   * A helper for returning the value mapped by a list of keys, ordered by priority.
   */
  public static int getInt(JSONObject object, List<String> keys) throws JSONException {
    for (String key : keys) {
      try {
        return object.getInt(key);
      } catch (JSONException e) {
        // do nothing
      }
    }
    throw new JSONException("No value for " + keys);
  }
}
