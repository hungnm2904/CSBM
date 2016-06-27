package com.csbm;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;

import com.csbm.http.BENetworkInterceptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

/**
 * The {@code CSBM} class contains static functions that handle global configuration for the CSBM
 * library.
 */
public class CSBM {
  /**
   * Represents an opaque configuration for the {@code CSBM} SDK configuration.
   */
  public static final class Configuration {
    /**
     * Allows for simple constructing of a {@code Configuration} object.
     */
    public static final class Builder {
      private Context context;
      private String applicationId;
      private String clientKey;
//      private String server = "https://api.parse.com/1/";
      private String server = "http://192.168.0.105:1337/parse/";
      private boolean localDataStoreEnabled;
      private List<BENetworkInterceptor> interceptors;

      /**
       * Initialize a bulider with a given context.
       *
       * This context will then be passed through to the rest of the CSBM SDK for use during
       * initialization.
       *
       * <p/>
       * You may define {@code com.csbm.APPLICATION_ID} and {@code com.csbm.CLIENT_KEY}
       * {@code meta-data} in your {@code AndroidManifest.xml}:
       * <pre>
       * &lt;manifest ...&gt;
       *
       * ...
       *
       *   &lt;application ...&gt;
       *     &lt;meta-data
       *       android:name="com.csbm.APPLICATION_ID"
       *       android:value="@string/csbm_app_id" /&gt;
       *     &lt;meta-data
       *       android:name="com.csbm.CLIENT_KEY"
       *       android:value="@string/csbm_client_key" /&gt;
       *
       *       ...
       *
       *   &lt;/application&gt;
       * &lt;/manifest&gt;
       * </pre>
       * <p/>
       *
       * This will cause the values for {@code applicationId} and {@code clientKey} to be set to
       * those defined in your manifest.
       *
       * @param context The active {@link Context} for your application. Cannot be null.
       */
      public Builder(Context context) {
        this.context = context;

        // Yes, our public API states we cannot be null. But for unit tests, it's easier just to
        // support null here.
        if (context != null) {
          Context applicationContext = context.getApplicationContext();
          Bundle metaData = ManifestInfo.getApplicationMetadata(applicationContext);
          if (metaData != null) {
            applicationId = metaData.getString(CSBM_APPLICATION_ID);
            clientKey = metaData.getString(CSBM_CLIENT_KEY);
          }
        }
      }

      /**
       * Set the application id to be used by CSBM.
       *
       * This method is only required if you intend to use a different {@code applicationId} than
       * is defined by {@code com.csbm.APPLICATION_ID} in your {@code AndroidManifest.xml}.
       *
       * @param applicationId The application id to set.
       * @return The same builder, for easy chaining.
       */
      public Builder applicationId(String applicationId) {
        this.applicationId = applicationId;
        return this;
      }

      /**
       * Set the client key to be used by CSBM.
       *
       * This method is only required if you intend to use a different {@code clientKey} than
       * is defined by {@code com.csbm.CLIENT_KEY} in your {@code AndroidManifest.xml}.
       *
       * @param clientKey The client key to set.
       * @return The same builder, for easy chaining.
       */
      public Builder clientKey(String clientKey) {
        this.clientKey = clientKey;
        return this;
      }

      /**
       * Set the server URL to be used by CSBM.
       *
       * This method is only required if you intend to use a different API server than the one at
       * api.parse.com.
       *
       * @param server The server URL to set.
       * @return The same builder, for easy chaining.
       */
      public Builder server(String server) {

        // Add an extra trailing slash so that csbm REST commands include
        // the path as part of the server URL (i.e. http://api.myhost.com/parse)
        if (server.endsWith("/") == false) {
          server = server + "/";
        }

        this.server = server;
        return this;
      }

      /**
       * Add a {@link BENetworkInterceptor}.
       *
       * @param interceptor The interceptor to add.
       * @return The same builder, for easy chaining.
       */
      public Builder addNetworkInterceptor(BENetworkInterceptor interceptor) {
        if (interceptors == null) {
          interceptors = new ArrayList<>();
        }
        interceptors.add(interceptor);
        return this;
      }

      /**
       * Enable pinning in your application. This must be called before your application can use
       * pinning.
       * @return The same builder, for easy chaining.
       */
      public Builder enableLocalDataStore() {
        localDataStoreEnabled = true;
        return this;
      }

      /* package for tests */ Builder setNetworkInterceptors(Collection<BENetworkInterceptor> interceptors) {
        if (this.interceptors == null) {
          this.interceptors = new ArrayList<>();
        } else {
          this.interceptors.clear();
        }

        if (interceptors != null) {
          this.interceptors.addAll(interceptors);
        }
        return this;
      }

      private Builder setLocalDatastoreEnabled(boolean enabled) {
        localDataStoreEnabled = enabled;
        return this;
      }

      /**
       * Construct this builder into a concrete {@code Configuration} instance.
       * @return A constructed {@code Configuration} object.
       */
      public Configuration build() {
        return new Configuration(this);
      }
    }

    /* package for tests */ final Context context;
    /* package for tests */ final String applicationId;
    /* package for tests */ final String clientKey;
    /* package for tests */ final String server;
    /* package for tests */ final boolean localDataStoreEnabled;
    /* package for tests */ final List<BENetworkInterceptor> interceptors;

    private Configuration(Builder builder) {
      this.context = builder.context;
      this.applicationId = builder.applicationId;
      this.clientKey = builder.clientKey;
      this.server = builder.server;
      this.localDataStoreEnabled = builder.localDataStoreEnabled;
      this.interceptors = builder.interceptors != null ?
        Collections.unmodifiableList(new ArrayList<>(builder.interceptors)) :
        null;
    }
  }

  private static final String CSBM_APPLICATION_ID = "com.csbm.APPLICATION_ID";
  private static final String CSBM_CLIENT_KEY = "com.csbm.CLIENT_KEY";

  private static final Object MUTEX = new Object();
  /* package */ static BEEventuallyQueue eventuallyQueue = null;

  //region LDS

  private static boolean isLocalDatastoreEnabled;
  private static OfflineStore offlineStore;

  /**
   * Enable pinning in your application. This must be called before your application can use
   * pinning. You must invoke {@code enableLocalDatastore(Context)} before
   * {@link #initialize(Context)} :
   * <p/>
   * <pre>
   * public class MyApplication extends Application {
   *   public void onCreate() {
   *     CSBM.enableLocalDatastore(this);
   *     CSBM.initialize(this);
   *   }
   * }
   * </pre>
   *
   * @param context
   *          The active {@link Context} for your application.
   */
  public static void enableLocalDatastore(Context context) {
    if (isInitialized()) {
      throw new IllegalStateException("`CSBM#enableLocalDatastore(Context)` must be invoked " +
          "before `CSBM#initialize(Context)`");
    }
    isLocalDatastoreEnabled = true;
  }

  /* package for tests */ static void disableLocalDatastore() {
    setLocalDatastore(null);
    // We need to re-register BECurrentInstallationController otherwise it is still offline
    // controller
    BECorePlugins.getInstance().reset();
  }

  /* package */ static OfflineStore getLocalDatastore() {
    return offlineStore;
  }

  /* package for tests */ static void setLocalDatastore(OfflineStore offlineStore) {
    CSBM.isLocalDatastoreEnabled = offlineStore != null;
    CSBM.offlineStore = offlineStore;
  }

  /* package */ static boolean isLocalDatastoreEnabled() {
    return isLocalDatastoreEnabled;
  }

  //endregion

  /**
   * Authenticates this client as belonging to your application.
   * <p/>
   * You must define {@code com.csbm.APPLICATION_ID} and {@code com.csbm.CLIENT_KEY}
   * {@code meta-data} in your {@code AndroidManifest.xml}:
   * <pre>
   * &lt;manifest ...&gt;
   *
   * ...
   *
   *   &lt;application ...&gt;
   *     &lt;meta-data
   *       android:name="com.csbm.APPLICATION_ID"
   *       android:value="@string/csbm_app_id" /&gt;
   *     &lt;meta-data
   *       android:name="com.csbm.CLIENT_KEY"
   *       android:value="@string/csbm_client_key" /&gt;
   *
   *       ...
   *
   *   &lt;/application&gt;
   * &lt;/manifest&gt;
   * </pre>
   * <p/>
   * This must be called before your application can use the CSBM library.
   * The recommended way is to put a call to {@code CSBM.initialize}
   * in your {@code Application}'s {@code onCreate} method:
   * <p/>
   * <pre>
   * public class MyApplication extends Application {
   *   public void onCreate() {
   *     CSBM.initialize(this);
   *   }
   * }
   * </pre>
   *
   * @param context
   *          The active {@link Context} for your application.
   */
  public static void initialize(Context context) {
    Configuration.Builder builder = new Configuration.Builder(context);
    if (builder.applicationId == null) {
      throw new RuntimeException("ApplicationId not defined. " +
        "You must provide ApplicationId in AndroidManifest.xml.\n" +
        "<meta-data\n" +
        "    android:name=\"com.csbm.APPLICATION_ID\"\n" +
        "    android:value=\"<Your Application Id>\" />");
    } if (builder.clientKey == null) {
      throw new RuntimeException("ClientKey not defined. " +
        "You must provide ClientKey in AndroidManifest.xml.\n" +
        "<meta-data\n" +
        "    android:name=\"com.csbm.CLIENT_KEY\"\n" +
        "    android:value=\"<Your Client Key>\" />");
    }
    initialize(builder.setNetworkInterceptors(interceptors)
        .setLocalDatastoreEnabled(isLocalDatastoreEnabled)
        .build()
    );
  }

  /**
   * Authenticates this client as belonging to your application.
   * <p/>
   * This method is only required if you intend to use a different {@code applicationId} or
   * {@code clientKey} than is defined by {@code com.csbm.APPLICATION_ID} or
   * {@code com.csbm.CLIENT_KEY} in your {@code AndroidManifest.xml}.
   * <p/>
   * This must be called before your
   * application can use the csbm library. The recommended way is to put a call to
   * {@code CSBM.initialize} in your {@code Application}'s {@code onCreate} method:
   * <p/>
   * <pre>
   * public class MyApplication extends Application {
   *   public void onCreate() {
   *     CSBM.initialize(this, &quot;your application id&quot;, &quot;your client key&quot;);
   *   }
   * }
   * </pre>
   *
   * @param context
   *          The active {@link Context} for your application.
   * @param applicationId
   *          The application id provided in the CSBM dashboard.
   * @param clientKey
   *          The client key provided in the CSBM dashboard.
   */
  public static void initialize(Context context, String applicationId, String clientKey) {
    initialize(new Configuration.Builder(context)
        .applicationId(applicationId)
        .clientKey(clientKey)
        .setNetworkInterceptors(interceptors)
        .setLocalDatastoreEnabled(isLocalDatastoreEnabled)
        .build()
    );
  }

  public static void initialize(Configuration configuration) {
    // NOTE (richardross): We will need this here, as BEPlugins uses the return value of
    // isLocalDataStoreEnabled() to perform additional behavior.
    isLocalDatastoreEnabled = configuration.localDataStoreEnabled;

    BEPlugins.Android.initialize(configuration.context, configuration.applicationId, configuration.clientKey);

    try {
      BERESTCommand.server = new URL(configuration.server);
    } catch (MalformedURLException ex) {
      throw new RuntimeException(ex);
    }

    Context applicationContext = configuration.context.getApplicationContext();

    BEHttpClient.setKeepAlive(true);
    BEHttpClient.setMaxConnections(20);
    // If we have interceptors in list, we have to initialize all http clients and add interceptors
    if (configuration.interceptors != null && configuration.interceptors.size() > 0) {
      initializeBEHttpClientsWithBENetworkInterceptors(configuration.interceptors);
    }

    BEObject.registerBESubclasses();

    if (configuration.localDataStoreEnabled) {
      offlineStore = new OfflineStore(configuration.context);
    } else {
      BEKeyValueCache.initialize(configuration.context);
    }

    // Make sure the data on disk for CSBM is for the current
    // application.
    checkCacheApplicationId();
    final Context context = configuration.context;
    Task.callInBackground(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        getEventuallyQueue(context);
        return null;
      }
    });

    BEFieldOperations.registerDefaultDecoders();

    if (!allBEPushIntentReceiversInternal()) {
      throw new SecurityException("To prevent external tampering to your app's notifications, " +
              "all receivers registered to handle the following actions must have " +
              "their exported attributes set to false: com.csbm.push.intent.RECEIVE, "+
              "com.csbm.push.intent.OPEN, com.csbm.push.intent.DELETE");
    }

    // May need to update GCM registration ID if app version has changed.
    // This also primes current installation.
    GcmRegistrar.getInstance().registerAsync().continueWithTask(new Continuation<Void, Task<Void>>() {
      @Override
      public Task<Void> then(Task<Void> task) throws Exception {
        // Prime current user in the background
        return BEUser.getCurrentUserAsync().makeVoid();
      }
    }).continueWith(new Continuation<Void, Void>() {
      @Override
      public Void then(Task<Void> task) throws Exception {
        // Prime config in the background
        BEConfig.getCurrentConfig();
        return null;
      }
    }, Task.BACKGROUND_EXECUTOR);

    if (ManifestInfo.getPushType() == PushType.PPNS) {
      PushService.startServiceIfRequired(applicationContext);
    }

    dispatchOnBEInitialized();

    // FYI we probably don't want to do this if we ever add other callbacks.
    synchronized (MUTEX_CALLBACKS) {
      CSBM.callbacks = null;
    }
  }

  /* package */ static void destroy() {
    BEEventuallyQueue queue;
    synchronized (MUTEX) {
      queue = eventuallyQueue;
      eventuallyQueue = null;
    }
    if (queue != null) {
      queue.onDestroy();
    }

    BECorePlugins.getInstance().reset();
    BEPlugins.reset();
  }

  /**
   * @return {@code True} if {@link #initialize} has been called, otherwise {@code false}.
   */
  /* package */ static boolean isInitialized() {
    return BEPlugins.get() != null;
  }

  static Context getApplicationContext() {
    checkContext();
    return BEPlugins.Android.get().applicationContext();
  }

  /**
   * Checks that each of the receivers associated with the three actions defined in
   * BEPushBroadcastReceiver (ACTION_PUSH_RECEIVE, ACTION_PUSH_OPEN, ACTION_PUSH_DELETE) has
   * their exported attributes set to false. If this is the case for each of the receivers
   * registered in the AndroidManifest.xml or if no receivers are registered (because we will be registering
   * the default implementation of BEPushBroadcastReceiver in PushService) then true is returned.
   * Note: the reason for iterating through lists, is because you can define different receivers
   * in the manifest that respond to the same intents and both all of the receivers will be triggered.
   * So we want to make sure all them have the exported attribute set to false.
   */
  private static boolean allBEPushIntentReceiversInternal() {
    List<ResolveInfo> intentReceivers = ManifestInfo.getIntentReceivers(
        BEPushBroadcastReceiver.ACTION_PUSH_RECEIVE,
        BEPushBroadcastReceiver.ACTION_PUSH_DELETE,
        BEPushBroadcastReceiver.ACTION_PUSH_OPEN);

    for (ResolveInfo resolveInfo : intentReceivers) {
      if (resolveInfo.activityInfo.exported) {
        return false;
      }
    }
    return true;
  }

  /**
   * @deprecated Please use {@link #getBECacheDir(String)} or {@link #getBEFilesDir(String)}
   * instead.
   */
  @Deprecated
  /* package */ static File getBEDir() {
    return BEPlugins.get().getBEDir();
  }

  /* package */ static File getBECacheDir() {
    return BEPlugins.get().getCacheDir();
  }

  /* package */ static File getBECacheDir(String subDir) {
    synchronized (MUTEX) {
      File dir = new File(getBECacheDir(), subDir);
      if (!dir.exists()) {
        dir.mkdirs();
      }
      return dir;
    }
  }

  /* package */ static File getBEFilesDir() {
    return BEPlugins.get().getFilesDir();
  }

  /* package */ static File getBEFilesDir(String subDir) {
    synchronized (MUTEX) {
      File dir = new File(getBEFilesDir(), subDir);
      if (!dir.exists()) {
        dir.mkdirs();
      }
      return dir;
    }
  }

  /**
   * Verifies that the data stored on disk for CSBM was generated using the same application that
   * is running now.
   */
  static void checkCacheApplicationId() {
    synchronized (MUTEX) {
      String applicationId = BEPlugins.get().applicationId();
      if (applicationId != null) {
        File dir = CSBM.getBECacheDir();

        // Make sure the current version of the cache is for this application id.
        File applicationIdFile = new File(dir, "applicationId");
        if (applicationIdFile.exists()) {
          // Read the file
          boolean matches = false;
          try {
            RandomAccessFile f = new RandomAccessFile(applicationIdFile, "r");
            byte[] bytes = new byte[(int) f.length()];
            f.readFully(bytes);
            f.close();
            String diskApplicationId = new String(bytes, "UTF-8");
            matches = diskApplicationId.equals(applicationId);
          } catch (FileNotFoundException e) {
            // Well, it existed a minute ago. Let's assume it doesn't match.
          } catch (IOException e) {
            // Hmm, the applicationId file was malformed or something. Assume it
            // doesn't match.
          }

          // The application id has changed, so everything on disk is invalid.
          if (!matches) {
            try {
              BEFileUtils.deleteDirectory(dir);
            } catch (IOException e) {
              // We're unable to delete the directy...
            }
          }
        }

        // Create the version file if needed.
        applicationIdFile = new File(dir, "applicationId");
        try {
          FileOutputStream out = new FileOutputStream(applicationIdFile);
          out.write(applicationId.getBytes("UTF-8"));
          out.close();
        } catch (FileNotFoundException e) {
          // Nothing we can really do about it.
        } catch (UnsupportedEncodingException e) {
          // Nothing we can really do about it. This would mean Java doesn't
          // understand UTF-8, which is unlikely.
        } catch (IOException e) {
          // Nothing we can really do about it.
        }
      }
    }
  }

  /**
   * Gets the shared command cache object for all BEObjects. This command cache is used to
   * locally store save commands created by the BEObject.saveEventually(). When a new
   * BECommandCache is instantiated, it will begin running its run loop, which will start by
   * processing any commands already stored in the on-disk queue.
   */
  /* package */ static BEEventuallyQueue getEventuallyQueue() {
    Context context = BEPlugins.Android.get().applicationContext();
    return getEventuallyQueue(context);
  }

  private static BEEventuallyQueue getEventuallyQueue(Context context) {
    synchronized (MUTEX) {
      boolean isLocalDatastoreEnabled = CSBM.isLocalDatastoreEnabled();
      if (eventuallyQueue == null
          || (isLocalDatastoreEnabled && eventuallyQueue instanceof BECommandCache)
          || (!isLocalDatastoreEnabled && eventuallyQueue instanceof BEPinningEventuallyQueue)) {
        checkContext();
        BEHttpClient httpClient = BEPlugins.get().restClient();
        eventuallyQueue = isLocalDatastoreEnabled
          ? new BEPinningEventuallyQueue(context, httpClient)
          : new BECommandCache(context, httpClient);

        // We still need to clear out the old command cache even if we're using Pinning in case
        // anything is left over when the user upgraded. Checking number of pending and then
        // initializing should be enough.
        if (isLocalDatastoreEnabled && BECommandCache.getPendingCount() > 0) {
          new BECommandCache(context, httpClient);
        }
      }
      return eventuallyQueue;
    }
  }

  static void checkInit() {
    if (BEPlugins.get() == null) {
      throw new RuntimeException("You must call CSBM.initialize(Context)"
          + " before using the CSBM library.");
    }

    if (BEPlugins.get().applicationId() == null) {
      throw new RuntimeException("applicationId is null. "
          + "You must call CSBM.initialize(Context)"
          + " before using the CSBM library.");
    }
    if (BEPlugins.get().clientKey() == null) {
      throw new RuntimeException("clientKey is null. "
          + "You must call BE.initialize(Context)"
          + " before using the CSBM library.");
    }
  }

  static void checkContext() {
    if (BEPlugins.Android.get().applicationContext() == null) {
      throw new RuntimeException("applicationContext is null. "
          + "You must call CSBM.initialize(Context)"
          + " before using the CSBM library.");
    }
  }

  static boolean hasPermission(String permission) {
    return (getApplicationContext().checkCallingOrSelfPermission(permission) ==
        PackageManager.PERMISSION_GRANTED);
  }

  static void requirePermission(String permission) {
    if (!hasPermission(permission)) {
      throw new IllegalStateException(
          "To use this functionality, add this to your AndroidManifest.xml:\n"
              + "<uses-permission android:name=\"" + permission + "\" />");
    }
  }

  //region BECallbacks
  private static final Object MUTEX_CALLBACKS = new Object();
  private static Set<BECallbacks> callbacks = new HashSet<>();

  /**
   * Registers a listener to be called at the completion of {@link #initialize}.
   * <p>
   * Throws {@link IllegalStateException} if called after {@link #initialize}.
   *
   * @param listener the listener to register
   */
  /* package */ static void registerBECallbacks(BECallbacks listener) {
    if (isInitialized()) {
      throw new IllegalStateException(
          "You must register callbacks before CSBM.initialize(Context)");
    }

    synchronized (MUTEX_CALLBACKS) {
      if (callbacks == null) {
        return;
      }
      callbacks.add(listener);
    }
  }

  /**
   * Unregisters a listener previously registered with {@link #registerBECallbacks}.
   *
   * @param listener the listener to register
   */
  /* package */ static void unregisterBECallbacks(BECallbacks listener) {
    synchronized (MUTEX_CALLBACKS) {
      if (callbacks == null) {
        return;
      }
      callbacks.remove(listener);
    }
  }

  private static void dispatchOnBEInitialized() {
    BECallbacks[] callbacks = collectBECallbacks();
    if (callbacks != null) {
      for (BECallbacks callback : callbacks) {
        callback.onBEInitialized();
      }
    }
  }

  private static BECallbacks[] collectBECallbacks() {
    BECallbacks[] callbacks;
    synchronized (MUTEX_CALLBACKS) {
      if (CSBM.callbacks == null) {
        return null;
      }
      callbacks = new BECallbacks[CSBM.callbacks.size()];
      if (CSBM.callbacks.size() > 0) {
        callbacks = CSBM.callbacks.toArray(callbacks);
      }
    }
    return callbacks;
  }

  /* package */ interface BECallbacks {
    public void onBEInitialized();
  }

  //endregion

  //region Logging

  public static final int LOG_LEVEL_VERBOSE = Log.VERBOSE;
  public static final int LOG_LEVEL_DEBUG = Log.DEBUG;
  public static final int LOG_LEVEL_INFO = Log.INFO;
  public static final int LOG_LEVEL_WARNING = Log.WARN;
  public static final int LOG_LEVEL_ERROR = Log.ERROR;
  public static final int LOG_LEVEL_NONE = Integer.MAX_VALUE;

  /**
   * Sets the level of logging to display, where each level includes all those below it. The default
   * level is {@link #LOG_LEVEL_NONE}. Please ensure this is set to {@link #LOG_LEVEL_ERROR}
   * or {@link #LOG_LEVEL_NONE} before deploying your app to ensure no sensitive information is
   * logged. The levels are:
   * <ul>
   * <li>{@link #LOG_LEVEL_VERBOSE}</li>
   * <li>{@link #LOG_LEVEL_DEBUG}</li>
   * <li>{@link #LOG_LEVEL_INFO}</li>
   * <li>{@link #LOG_LEVEL_WARNING}</li>
   * <li>{@link #LOG_LEVEL_ERROR}</li>
   * <li>{@link #LOG_LEVEL_NONE}</li>
   * </ul>
   *
   * @param logLevel
   *          The level of logcat logging that CSBM should do.
   */
  public static void setLogLevel(int logLevel) {
    PLog.setLogLevel(logLevel);
  }

  /**
   * Returns the level of logging that will be displayed.
   */
  public static int getLogLevel() {
    return PLog.getLogLevel();
  }

  //endregion

  // Suppress constructor to prevent subclassing
  private CSBM() {
    throw new AssertionError();
  }

  private static List<BENetworkInterceptor> interceptors;

  // Initialize all necessary http clients and add interceptors to these http clients
  private static void initializeBEHttpClientsWithBENetworkInterceptors(List<BENetworkInterceptor> interceptors) {
    // This means developers have not called addInterceptor method so we should do nothing.
    if (interceptors == null) {
      return;
    }

    List<BEHttpClient> clients = new ArrayList<>();

    // Rest http client
    clients.add(BEPlugins.get().restClient());
    // AWS http client
    clients.add(BECorePlugins.getInstance().getFileController().awsClient());

    // Add interceptors to http clients
    for (BEHttpClient beHttpClient : clients) {
      // We need to add the decompress interceptor before the external interceptors to return
      // a decompressed response to CSBM.
      beHttpClient.addInternalInterceptor(new BEDecompressInterceptor());
      for (BENetworkInterceptor interceptor : interceptors) {
        beHttpClient.addExternalInterceptor(interceptor);
      }
    }
  }


  /**
   * Add a {@link BENetworkInterceptor}. You must invoke
   * {@code addBENetworkInterceptor(BENetworkInterceptor)} before
   * {@link #initialize(Context)}. You can add multiple {@link BENetworkInterceptor}.
   *
   * @param interceptor
   *          {@link BENetworkInterceptor} to be added.
   */
  public static void addBENetworkInterceptor(BENetworkInterceptor interceptor) {
    if (isInitialized()) {
      throw new IllegalStateException("`CSBM#addBENetworkInterceptor(BENetworkInterceptor)`"
          + " must be invoked before `CSBM#initialize(Context)`");
    }
    if (interceptors == null) {
      interceptors = new ArrayList<>();
    }
    interceptors.add(interceptor);
  }

  /**
   * Remove a given {@link BENetworkInterceptor}. You must invoke
   * {@code removeBENetworkInterceptor(BENetworkInterceptor)}  before
   * {@link #initialize(Context)}.
   *
   * @param interceptor
   *          {@link BENetworkInterceptor} to be removed.
   */
  public static void removeBENetworkInterceptor(BENetworkInterceptor interceptor) {
    if (isInitialized()) {
      throw new IllegalStateException("`CSBM#addBENetworkInterceptor(BENetworkInterceptor)`"
          + " must be invoked before `CSBM#initialize(Context)`");
    }
    if (interceptors == null) {
      return;
    }
    interceptors.remove(interceptor);
  }

  /* package */ static String externalVersionName() {
    return "a" + BEObject.VERSION_NAME;
  }
}
