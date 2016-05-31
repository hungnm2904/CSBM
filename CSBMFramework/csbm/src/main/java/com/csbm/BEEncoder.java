/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.csbm;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A {@code BEEncoder} can be used to transform objects such as {@link BEObjects} into JSON
 * data structures.
 *
 * @see com.csbm.BEDecoder
 */

/** package */ abstract class BEEncoder {

  /* package */ static boolean isValidType(Object value) {
    return value instanceof String
        || value instanceof Number
        || value instanceof Boolean
        || value instanceof Date
        || value instanceof List
        || value instanceof Map
        || value instanceof byte[]
        || value == JSONObject.NULL
        || value instanceof BEObject
        || value instanceof BEACL
        || value instanceof BEFile
        || value instanceof BEGeoPoint
        || value instanceof BERelation;
  }

  public Object encode(Object object) {
    try {
      if (object instanceof BEObject) {
        return encodeRelatedObject((BEObject) object);
      }

      // TODO(grantland): Remove once we disallow mutable nested queries t6941155
      if (object instanceof BEQuery.State.Builder<?>) {
        BEQuery.State.Builder<?> builder = (BEQuery.State.Builder<?>) object;
        return encode(builder.build());
      }

      if (object instanceof BEQuery.State<?>) {
        BEQuery.State<?> state = (BEQuery.State<?>) object;
        return state.toJSON(this);
      }

      if (object instanceof Date) {
        return encodeDate((Date) object);
      }

      if (object instanceof byte[]) {
        JSONObject json = new JSONObject();
        json.put("__type", "Bytes");
        json.put("base64", Base64.encodeToString((byte[]) object, Base64.NO_WRAP));
        return json;
      }

      if (object instanceof BEFile) {
        return ((BEFile) object).encode();
      }

      if (object instanceof BEGeoPoint) {
        BEGeoPoint point = (BEGeoPoint) object;
        JSONObject json = new JSONObject();
        json.put("__type", "GeoPoint");
        json.put("latitude", point.getLatitude());
        json.put("longitude", point.getLongitude());
        return json;
      }

      if (object instanceof BEACL) {
        BEACL acl = (BEACL) object;
        return acl.toJSONObject(this);
      }

      if (object instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) object;
        JSONObject json = new JSONObject();
        for (Map.Entry<String, Object> pair : map.entrySet()) {
          json.put(pair.getKey(), encode(pair.getValue()));
        }
        return json;
      }

      if (object instanceof Collection) {
        JSONArray array = new JSONArray();
        for (Object item : (Collection<?>) object) {
          array.put(encode(item));
        }
        return array;
      }

      if (object instanceof BERelation) {
        BERelation<?> relation = (BERelation<?>) object;
        return relation.encodeToJSON(this);
      }

      if (object instanceof BEFieldOperation) {
        return ((BEFieldOperation) object).encode(this);
      }

      if (object instanceof BEQuery.RelationConstraint) {
        return ((BEQuery.RelationConstraint) object).encode(this);
      }

      if (object == null) {
        return JSONObject.NULL;
      }

    } catch (JSONException e) {
      throw new RuntimeException(e);
    }

    // String, Number, Boolean,
    if (isValidType(object)) {
      return object;
    }

    throw new IllegalArgumentException("invalid type for BEObject: "
        + object.getClass().toString());
  }

  protected abstract JSONObject encodeRelatedObject(BEObject object);

  protected JSONObject encodeDate(Date date) {
    JSONObject object = new JSONObject();
    String iso = BEDateFormat.getInstance().format(date);
    try {
      object.put("__type", "Date");
      object.put("iso", iso);
    } catch (JSONException e) {
      // This should not happen
      throw new RuntimeException(e);
    }
    return object;
  }
}
