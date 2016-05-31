package com.csbm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * Since we cannot save dirty BEObjects to disk and we must be able to persist UUIDs across
 * restarts even if the BEInstallation is not saved, we use this legacy file still as a
 * boostrapping environment as well until the full BEInstallation is cached to disk.
 *
 * TODO: Allow dirty objects to be saved to disk.
 */
/* package */ class InstallationId {

  private static final String TAG = "InstallationId";

  private final Object lock = new Object();
  private final File file;
  private String installationId;

  public InstallationId(File file) {
    this.file = file;
  }

  /**
   * Loads the installationId from memory, then tries to loads the legacy installationId from disk
   * if it is present, or creates a new random UUID.
   */
  public String get() {
    synchronized (lock) {
      if (installationId == null) {
        try {
          installationId = BEFileUtils.readFileToString(file, "UTF-8");
        } catch (FileNotFoundException e) {
          PLog.i(TAG, "Couldn't find existing installationId file. Creating one instead.");
        } catch (IOException e) {
          PLog.e(TAG, "Unexpected exception reading installation id from disk", e);
        }
      }

      if (installationId == null) {
        setInternal(UUID.randomUUID().toString());
      }
    }

    return installationId;
  }

  /**
   * Sets the installationId and persists it to disk.
   */
  public void set(String newInstallationId) {
    synchronized (lock) {
      if (BETextUtils.isEmpty(newInstallationId)
        || newInstallationId.equals(get())) {
        return;
      }
      setInternal(newInstallationId);
    }
  }

  private void setInternal(String newInstallationId) {
    synchronized (lock) {
      try {
        BEFileUtils.writeStringToFile(file, newInstallationId, "UTF-8");
      } catch (IOException e) {
        PLog.e(TAG, "Unexpected exception writing installation id to disk", e);
      }

      installationId = newInstallationId;
    }
  }

  /* package for tests */ void clear() {
    synchronized (lock) {
      installationId = null;
      BEFileUtils.deleteQuietly(file);
    }
  }
}
