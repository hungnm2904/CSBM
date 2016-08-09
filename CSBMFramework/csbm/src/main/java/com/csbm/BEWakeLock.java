package com.csbm;

import android.content.Context;
import android.os.PowerManager;

/**
 * Utility class that wraps a PowerManager.WakeLock and logs an error if the app doesn't have 
 * permissions to acquire wake locks.
 */

/** package */ class BEWakeLock {
  private static final String TAG = "com.csbm.BEWakeLock";

  private static volatile boolean hasWakeLockPermission = true;
  private final PowerManager.WakeLock wakeLock;

  public static BEWakeLock acquireNewWakeLock(Context context, int type, String reason, long timeout) {
    PowerManager.WakeLock wl = null;

    if (hasWakeLockPermission) {
      try {
        PowerManager pm = (PowerManager)context.getApplicationContext().getSystemService(Context.POWER_SERVICE);

        if (pm != null) {
          wl = pm.newWakeLock(type, reason);

          if (wl != null) {
            wl.setReferenceCounted(false);

            if (timeout == 0) {
              wl.acquire();
            } else {
              wl.acquire(timeout);
            }
          }
        }
      } catch (SecurityException e) {
        PLog.e(TAG, "Failed to acquire a PowerManager.WakeLock. This is" +
            "necessary for reliable handling of pushes. Please add this to your Manifest.xml: " +
            "<uses-permission android:name=\"android.permission.WAKE_LOCK\" /> ");

        hasWakeLockPermission = false;
        wl = null;
      }
    }

    return new BEWakeLock(wl);
  }

  private BEWakeLock(PowerManager.WakeLock wakeLock) {
    this.wakeLock = wakeLock;
  }
  
  public void release() {
    if (wakeLock != null) {
      wakeLock.release();
    }
  }
}
