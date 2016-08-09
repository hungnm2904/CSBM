package com.csbm;

import java.util.Collections;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/** package */ class BEPushChannelsController {
  private static final String TAG = "com.csbm.BEPushChannelsController";

  private static BECurrentInstallationController getCurrentInstallationController() {
    return BECorePlugins.getInstance().getCurrentInstallationController();
  }

  public Task<Void> subscribeInBackground(final String channel) {
    checkManifestAndLogErrorIfNecessary();
    if (channel == null) {
      throw new IllegalArgumentException("Can't subscribe to null channel.");
    }
    return getCurrentInstallationController().getAsync().onSuccessTask(new Continuation<BEInstallation, Task<Void>>() {
      @Override
      public Task<Void> then(Task<BEInstallation> task) throws Exception {
        BEInstallation installation = task.getResult();
        List<String> channels = installation.getList(BEInstallation.KEY_CHANNELS);
        if (channels == null
            || installation.isDirty(BEInstallation.KEY_CHANNELS)
            || !channels.contains(channel)) {
          installation.addUnique(BEInstallation.KEY_CHANNELS, channel);
          return installation.saveInBackground();
        } else {
          return Task.forResult(null);
        }
      }
    });
  }

  public Task<Void> unsubscribeInBackground(final String channel) {
    checkManifestAndLogErrorIfNecessary();
    if (channel == null) {
      throw new IllegalArgumentException("Can't unsubscribe from null channel.");
    }
    return getCurrentInstallationController().getAsync().onSuccessTask(new Continuation<BEInstallation, Task<Void>>() {
      @Override
      public Task<Void> then(Task<BEInstallation> task) throws Exception {
        BEInstallation installation = task.getResult();
        List<String> channels = installation.getList(BEInstallation.KEY_CHANNELS);
        if (channels != null && channels.contains(channel)) {
          installation.removeAll(
              BEInstallation.KEY_CHANNELS, Collections.singletonList(channel));
          return installation.saveInBackground();
        } else {
          return Task.forResult(null);
        }
      }
    });
  }

  private static boolean loggedManifestError = false;
  private static void checkManifestAndLogErrorIfNecessary() {
    if (!loggedManifestError && ManifestInfo.getPushType() == PushType.NONE) {
      loggedManifestError = true;
      PLog.e(TAG, "Tried to subscribe or unsubscribe from a channel, but push is not enabled " +
          "correctly. " + ManifestInfo.getNonePushTypeLogMessage());
    }
  }
}
