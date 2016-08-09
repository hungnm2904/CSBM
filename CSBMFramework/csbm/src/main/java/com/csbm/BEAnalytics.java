package com.csbm;

import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import bolts.Capture;
import bolts.Continuation;
import bolts.Task;

/**
 * The {@code BEAnalytics} class provides an interface to CSBM's logging and analytics backend.
 * Methods will return immediately and cache requests (+ timestamps) to be handled "eventually."
 * That is, the request will be sent immediately if possible or the next time a network connection
 * is available otherwise.
 */
public class BEAnalytics {
  private static final String TAG = "com.csbm.BEAnalytics";

  /* package for test */ static BEAnalyticsController getAnalyticsController() {
    return BECorePlugins.getInstance().getAnalyticsController();
  }

  /**
   * Tracks this application being launched (and if this happened as the result of the user opening
   * a push notification, this method sends along information to correlate this open with that
   * push).
   *
   * @param intent
   *          The {@code Intent} that started an {@code Activity}, if any. Can be null.
   * @return A Task that is resolved when the event has been tracked by CSBM.
   */
  public static Task<Void> trackAppOpenedInBackground(Intent intent) {
    String pushHashStr = getPushHashFromIntent(intent);
    final Capture<String> pushHash = new Capture<>();
    if (pushHashStr != null && pushHashStr.length() > 0) {
      synchronized (lruSeenPushes) {
        if (lruSeenPushes.containsKey(pushHashStr)) {
          return Task.forResult(null);
        } else {
          lruSeenPushes.put(pushHashStr, true);
          pushHash.set(pushHashStr);
        }
      }
    }
    return BEUser.getCurrentSessionTokenAsync().onSuccessTask(new Continuation<String, Task<Void>>() {
      @Override
      public Task<Void> then(Task<String> task) throws Exception {
        String sessionToken = task.getResult();
        return getAnalyticsController().trackAppOpenedInBackground(pushHash.get(), sessionToken);
      }
    });
  }

  /**
   * @deprecated Please use {@link #trackAppOpenedInBackground(Intent)} instead.
   */
  @Deprecated
  public static void trackAppOpened(Intent intent) {
    trackAppOpenedInBackground(intent);
  }

  /**
   * Tracks this application being launched (and if this happened as the result of the user opening
   * a push notification, this method sends along information to correlate this open with that
   * push).
   *
   * @param intent
   *          The {@code Intent} that started an {@code Activity}, if any. Can be null.
   * @param callback
   *          callback.done(e) is called when the event has been tracked by CSBM.
   */
  public static void trackAppOpenedInBackground(Intent intent, SaveCallback callback) {
    BETaskUtils.callbackOnMainThreadAsync(trackAppOpenedInBackground(intent), callback);
  }

  /**
   * @deprecated Please use {@link #trackEventInBackground(String)} instead.
   */
  @Deprecated
  public static void trackEvent(String name) {
    trackEventInBackground(name);
  }

  /**
   * Tracks the occurrence of a custom event. CSBM will store a data point at the time of
   * invocation with the given event name.
   *
   * @param name
   *          The name of the custom event to report to CSBM as having happened.
   * @param callback
   *          callback.done(e) is called when the event has been tracked by CSBM.
   */
  public static void trackEventInBackground(String name, SaveCallback callback) {
    BETaskUtils.callbackOnMainThreadAsync(trackEventInBackground(name), callback);
  }

  /**
   * @deprecated Please use {@link #trackEventInBackground(String, Map)} instead.
   */
  @Deprecated
  public static void trackEvent(String name, Map<String, String> dimensions) {
    trackEventInBackground(name, dimensions);
  }

  /**
   * Tracks the occurrence of a custom event with additional dimensions. CSBM will store a data
   * point at the time of invocation with the given event name.  Dimensions will allow segmentation
   * of the occurrences of this custom event.
   * <p>
   * To track a user signup along with additional metadata, consider the following:
   * <pre>
   * Map<String, String> dimensions = new HashMap<String, String>();
   * dimensions.put("gender", "m");
   * dimensions.put("source", "web");
   * dimensions.put("dayType", "weekend");
   * BEAnalytics.trackEvent("signup", dimensions);
   * </pre>
   * There is a default limit of 8 dimensions per event tracked.
   *
   * @param name
   *          The name of the custom event to report to CSBM as having happened.
   * @param dimensions
   *          The dictionary of information by which to segment this event.
   * @param callback
   *          callback.done(e) is called when the event has been tracked by CSBM.
   */
  public static void trackEventInBackground(String name, Map<String, String> dimensions, SaveCallback callback) {
    BETaskUtils.callbackOnMainThreadAsync(trackEventInBackground(name, dimensions), callback);
  }

  /**
   * Tracks the occurrence of a custom event with additional dimensions. CSBM will store a data
   * point at the time of invocation with the given event name.  Dimensions will allow segmentation
   * of the occurrences of this custom event.
   * <p>
   * To track a user signup along with additional metadata, consider the following:
   * <pre>
   * Map<String, String> dimensions = new HashMap<String, String>();
   * dimensions.put("gender", "m");
   * dimensions.put("source", "web");
   * dimensions.put("dayType", "weekend");
   * CSBMAnalytics.trackEvent("signup", dimensions);
   * </pre>
   * There is a default limit of 8 dimensions per event tracked.
   *
   * @param name
   *          The name of the custom event to report to CSBM as having happened.
   *
   * @return A Task that is resolved when the event has been tracked by CSBM.
   */
  public static Task<Void> trackEventInBackground(String name) {
    return trackEventInBackground(name, (Map<String, String>) null);
  }

  /**
   * Tracks the occurrence of a custom event with additional dimensions. CSBM will store a data
   * point at the time of invocation with the given event name.  Dimensions will allow segmentation
   * of the occurrences of this custom event.
   * <p>
   * To track a user signup along with additional metadata, consider the following:
   * <pre>
   * Map<String, String> dimensions = new HashMap<String, String>();
   * dimensions.put("gender", "m");
   * dimensions.put("source", "web");
   * dimensions.put("dayType", "weekend");
   * BEAnalytics.trackEvent("signup", dimensions);
   * </pre>
   * There is a default limit of 8 dimensions per event tracked.
   *
   * @param name
   *          The name of the custom event to report to CSBM as having happened.
   * @param dimensions
   *          The dictionary of information by which to segment this event.
   *
   * @return A Task that is resolved when the event has been tracked by CSBM.
   */
  public static Task<Void> trackEventInBackground(final String name,
                                                  Map<String, String> dimensions) {
    if (name == null || name.trim().length() == 0) {
      throw new IllegalArgumentException("A name for the custom event must be provided.");
    }
    final Map<String, String> dimensionsCopy = dimensions != null
        ? Collections.unmodifiableMap(new HashMap<>(dimensions))
        : null;

    return BEUser.getCurrentSessionTokenAsync().onSuccessTask(new Continuation<String, Task<Void>>() {
      @Override
      public Task<Void> then(Task<String> task) throws Exception {
        String sessionToken = task.getResult();
        return getAnalyticsController().trackEventInBackground(name, dimensionsCopy, sessionToken);
      }
    });
  }

  // Developers have the option to manually track push opens or the app open event can be tracked
  // automatically by the BEPushBroadcastReceiver. To avoid double-counting a push open, we track
  // the pushes we've seen locally. We don't need to worry about doing this in any sort of durable
  // way because a push can only launch the app once.
  private static final Map<String, Boolean> lruSeenPushes = new LinkedHashMap<String, Boolean>() {
    protected boolean removeEldestEntry(Entry<String, Boolean> eldest) {
      return size() > 10;
    }
  };

  /* package */ static void clear() {
    synchronized (lruSeenPushes) {
      lruSeenPushes.clear();
    }
  }

  /* package for test */ static String getPushHashFromIntent(Intent intent) {
    String pushData = null;
    if (intent != null && intent.getExtras() != null) {
      pushData = intent.getExtras().getString(BEPushBroadcastReceiver.KEY_PUSH_DATA);
    }
    if (pushData == null) {
      return null;
    }
    String pushHash = null;
    try {
      JSONObject payload = new JSONObject(pushData);
      pushHash = payload.optString("push_hash");
    } catch (JSONException e) {
      PLog.e(TAG, "Failed to csbm push data: " + e.getMessage());
    }
    return pushHash;
  }
}
