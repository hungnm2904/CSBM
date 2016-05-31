package com.csbm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @exclude
 */
public class GcmBroadcastReceiver extends BroadcastReceiver {
  @Override
  public final void onReceive(Context context, Intent intent) {
    ServiceUtils.runWakefulIntentInService(context, intent, PushService.class);
  }
}
