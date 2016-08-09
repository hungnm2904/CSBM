package com.csbm;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

import static com.csbm.BEUser.State;

/**
 * Handles encoding/decoding BEUser to/from /2 format JSON. /2 format json is only used for
 * persisting current BEUser and BEInstallation to disk when LDS is not enabled.
 */

/** package */ class BEUserCurrentCoder extends BEObjectCurrentCoder {

  private static final String KEY_AUTH_DATA = "auth_data";
  private static final String KEY_SESSION_TOKEN = "session_token";

  private static final BEUserCurrentCoder INSTANCE = new BEUserCurrentCoder();
  public static BEUserCurrentCoder get() {
    return INSTANCE;
  }

  /* package */ BEUserCurrentCoder() {
    // do nothing
  }

  /**
   * Converts a BEUser state to /2/ JSON representation suitable for saving to disk.
   *
   * <pre>
   * {
   *   data: {
   *     // data fields, including objectId, createdAt, updatedAt
   *   },
   *   classname: class name for the object,
   *   operations: { } // operations per field
   * }
   * </pre>
   *
   * All keys are included, regardless of whether they are dirty.
   * We also add sessionToken and authData to the json.
   *
   * @see #decode(BEObject.State.Init, JSONObject, BEDecoder)
   */
  @Override
  public <T extends BEObject.State> JSONObject encode(
          T state, BEOperationSet operations, BEEncoder encoder) {
    // FYI we'll be double writing sessionToken and authData for now...
    JSONObject objectJSON = super.encode(state, operations, encoder);

    String sessionToken = ((State) state).sessionToken();
    if (sessionToken != null) {
      try {
        objectJSON.put(KEY_SESSION_TOKEN, sessionToken);
      } catch (JSONException e) {
        throw new RuntimeException("could not encode value for key: session_token");
      }
    }

    Map<String, Map<String, String>> authData = ((State) state).authData();
    if (authData.size() > 0) {
      try {
        objectJSON.put(KEY_AUTH_DATA, encoder.encode(authData));
      } catch (JSONException e) {
        throw new RuntimeException("could not attach key: auth_data");
      }
    }

    return objectJSON;
  }

  /**
   * Merges from JSON in /2/ format.
   *
   * This is only used to read BEUser state stored on disk in JSON.
   * Since in encode we add sessionToken and authData to the json, we need remove them from json
   * to generate state.
   *
   * @see #encode(BEObject.State, BEOperationSet, BEEncoder)
   */
  @Override
  public <T extends BEObject.State.Init<?>> T decode(
          T builder, JSONObject json, BEDecoder decoder) {
    BEUser.State.Builder userBuilder = (State.Builder) builder;
    String newSessionToken = json.optString(KEY_SESSION_TOKEN, null);
    if (newSessionToken != null) {
      userBuilder.sessionToken(newSessionToken);
      json.remove(KEY_SESSION_TOKEN);
    }

    JSONObject newAuthData = json.optJSONObject(KEY_AUTH_DATA);
    if (newAuthData != null) {
      try {
        // Merge in auth data.
        @SuppressWarnings("rawtypes")
        Iterator i = newAuthData.keys();
        while (i.hasNext()) {
          String key = (String) i.next();
          if (!newAuthData.isNull(key)) {
            userBuilder.putAuthData(key,
                (Map<String, String>) BEDecoder.get().decode(newAuthData.getJSONObject(key)));
          }
        }
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
      json.remove(KEY_AUTH_DATA);
    }

    // FYI we'll be double writing sessionToken and authData for now...
    return super.decode(builder, json, decoder);
  }
}
