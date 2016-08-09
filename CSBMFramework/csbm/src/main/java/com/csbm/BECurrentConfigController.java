package com.csbm;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import bolts.Task;

/** package */ class BECurrentConfigController {

  private final Object currentConfigMutex = new Object();
  /* package for test */ BEConfig currentConfig;
  private File currentConfigFile;

  public BECurrentConfigController(File currentConfigFile) {
    this.currentConfigFile = currentConfigFile;
  }

  public Task<Void> setCurrentConfigAsync(final BEConfig config) {
    return Task.call(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        synchronized (currentConfigMutex) {
          currentConfig = config;
          saveToDisk(config);
        }
        return null;
      }
    }, BEExecutors.io());
  }

  public Task<BEConfig> getCurrentConfigAsync() {
    return Task.call(new Callable<BEConfig>() {
      @Override
      public BEConfig call() throws Exception {
        synchronized (currentConfigMutex) {
          if (currentConfig == null) {
            BEConfig config = getFromDisk();
            currentConfig = (config != null) ? config : new BEConfig();
          }
        }
        return currentConfig;
      }
    }, BEExecutors.io());
  }

  /**
   * Retrieves a {@code BEConfig} from a file on disk.
   *
   * @return The {@code BEConfig} that was retrieved. If the file wasn't found, or the contents
   *          of the file is an invalid {@code BEConfig}, returns null.
   */
  /* package for test */ BEConfig getFromDisk() {
    JSONObject json;
    try {
      json = BEFileUtils.readFileToJSONObject(currentConfigFile);
    } catch (IOException | JSONException e) {
      return null;
    }
    return BEConfig.decode(json, BEDecoder.get());
  }

  /* package */ void clearCurrentConfigForTesting() {
    synchronized (currentConfigMutex) {
      currentConfig = null;
    }
  }

  /**
   * Saves the {@code BEConfig} to the a file on disk as JSON.
   *
   * @param config
   *          The BEConfig which needs to be saved.
   */
  /* package for test */ void saveToDisk(BEConfig config) {
    JSONObject object = new JSONObject();
    try {
      JSONObject jsonParams = (JSONObject) NoObjectsEncoder.get().encode(config.getParams());
      object.put("params", jsonParams);
    } catch (JSONException e) {
      throw new RuntimeException("could not serialize config to JSON");
    }
    try {
      BEFileUtils.writeJSONObjectToFile(currentConfigFile, object);
    } catch (IOException e) {
      //TODO (grantland): We should do something if this fails...
    }
  }
}
