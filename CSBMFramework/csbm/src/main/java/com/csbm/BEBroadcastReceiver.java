package com.csbm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @exclude
 */
public class BEBroadcastReceiver extends BroadcastReceiver {
  private static final String TAG = "com.csbm.BEBroadcastReceiver";

  @Override
  public void onReceive(Context context, Intent intent) {
    // This exists to restart the service when the device is first turned
    // on, or on other system events for which we want to ensure that the
    // push service is running.
    PLog.d(TAG, "received " + intent.getAction());
    PushService.startServiceIfRequired(context);
  }
}
