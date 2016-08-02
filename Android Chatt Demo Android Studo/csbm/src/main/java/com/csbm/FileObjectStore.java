package com.csbm;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import bolts.Task;

/** package */ class FileObjectStore<T extends BEObject> implements BEObjectStore<T> {

  private static BEObjectSubclassingController getSubclassingController() {
    return BECorePlugins.getInstance().getSubclassingController();
  }

  /**
   * Saves the {@code BEObject} to the a file on disk as JSON in /2/ format.
   *
   * @param coder
   *          Current coder to encode the BEObject.
   * @param current
   *          BEObject which needs to be saved to disk.
   * @param file
   *          The file to save the object to.
   *
   * @see #getFromDisk(BEObjectCurrentCoder, File, BEObject.State.Init)
   */
  private static void saveToDisk(
          BEObjectCurrentCoder coder, BEObject current, File file) {
    JSONObject json = coder.encode(current.getState(), null, PointerEncoder.get());
    try {
      BEFileUtils.writeJSONObjectToFile(file, json);
    } catch (IOException e) {
      //TODO(grantland): We should do something if this fails...
    }
  }

  /**
   * Retrieves a {@code BEObject} from a file on disk in /2/ format.
   *
   * @param coder
   *          Current coder to decode the BEObject.
   * @param file
   *          The file to retrieve the object from.
   * @param builder
   *          An empty builder which is used to generate a empty state and rebuild a BEObject.
   * @return The {@code BEObject} that was retrieved. If the file wasn't found, or the contents
   *         of the file is an invalid {@code BEObject}, returns {@code null}.
   *
   * @see #saveToDisk(BEObjectCurrentCoder, BEObject, File)
   */
  private static <T extends BEObject> T getFromDisk(
          BEObjectCurrentCoder coder, File file, BEObject.State.Init builder) {
    JSONObject json;
    try {
      json = BEFileUtils.readFileToJSONObject(file);
    } catch (IOException | JSONException e) {
      return null;
    }

    BEObject.State newState = coder.decode(builder, json, BEDecoder.get())
        .isComplete(true)
        .build();
    return BEObject.from(newState);
  }

  private final String className;
  private final File file;
  private final BEObjectCurrentCoder coder;

  public FileObjectStore(Class<T> clazz, File file, BEObjectCurrentCoder coder) {
    this(getSubclassingController().getClassName(clazz), file, coder);
  }

  public FileObjectStore(String className, File file, BEObjectCurrentCoder coder) {
    this.className = className;
    this.file = file;
    this.coder = coder;
  }

  @Override
  public Task<Void> setAsync(final T object) {
    return Task.call(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        saveToDisk(coder, object, file);
        //TODO (grantland): check to see if this failed? We currently don't for legacy reasons.
        return null;
      }
    }, BEExecutors.io());
  }

  @Override
  public Task<T> getAsync() {
    return Task.call(new Callable<T>() {
      @Override
      public T call() throws Exception {
        if (!file.exists()) {
          return null;
        }
        return getFromDisk(coder, file, BEObject.State.newBuilder(className));
      }
    }, BEExecutors.io());
  }

  @Override
  public Task<Boolean> existsAsync() {
    return Task.call(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return file.exists();
      }
    }, BEExecutors.io());
  }

  @Override
  public Task<Void> deleteAsync() {
    return Task.call(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        if (file.exists() && !BEFileUtils.deleteQuietly(file)) {
          throw new RuntimeException("Unable to delete");
        }

        return null;
      }
    }, BEExecutors.io());
  }
}
