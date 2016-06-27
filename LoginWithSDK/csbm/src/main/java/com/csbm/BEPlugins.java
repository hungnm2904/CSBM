/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.csbm;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.SSLSessionCache;
import android.os.Build;

import com.csbm.http.BEHttpRequest;
import com.csbm.http.BEHttpResponse;
import com.csbm.http.BENetworkInterceptor;

import java.io.File;
import java.io.IOException;

/** package */ class BEPlugins {

  private static final String INSTALLATION_ID_LOCATION = "installationId";

  private static final Object LOCK = new Object();
  private static BEPlugins instance;

  // TODO(grantland): Move towards a Config/Builder parameter pattern to allow other configurations
  // such as path (disabled for Android), etc.
  /* package */ static void initialize(String applicationId, String clientKey) {
    BEPlugins.set(new BEPlugins(applicationId, clientKey));
  }

  /* package for tests */ static void set(BEPlugins plugins) {
    synchronized (LOCK) {
      if (instance != null) {
        throw new IllegalStateException("BEPlugins is already initialized");
      }
      instance = plugins;
    }
  }

  /* package */ static BEPlugins get() {
    synchronized (LOCK) {
      return instance;
    }
  }

  /* package */ static void reset() {
    synchronized (LOCK) {
      instance = null;
    }
  }

  /* package */ final Object lock = new Object();
  private final String applicationId;
  private final String clientKey;

  private BEHttpClient restClient;
  private InstallationId installationId;

  /* package */ File beDir;
  /* package */ File cacheDir;
  /* package */ File filesDir;

  private BEPlugins(String applicationId, String clientKey) {
    this.applicationId = applicationId;
    this.clientKey = clientKey;
  }

  /* package */ String applicationId() {
    return applicationId;
  }

  /* package */ String clientKey() {
    return clientKey;
  }

  /* package */ BEHttpClient newHttpClient() {
    int socketOperationTimeout = 10 * 1000; // 10 seconds
    return BEHttpClient.createClient(
        socketOperationTimeout,
        null);
  }

  /* package */ BEHttpClient restClient() {
    synchronized (lock) {
      if (restClient == null) {
        restClient = newHttpClient();
        restClient.addInternalInterceptor(new BENetworkInterceptor() {
          @Override
          public BEHttpResponse intercept(Chain chain) throws IOException {
            BEHttpRequest request = chain.getRequest();
            BEHttpRequest.Builder builder = new BEHttpRequest.Builder(request)
                .addHeader(BERESTCommand.HEADER_APPLICATION_ID, applicationId)
                .addHeader(BERESTCommand.HEADER_CLIENT_KEY, clientKey)
                .addHeader(BERESTCommand.HEADER_CLIENT_VERSION, CSBM.externalVersionName())
                .addHeader(
                    BERESTCommand.HEADER_APP_BUILD_VERSION,
                    String.valueOf(ManifestInfo.getVersionCode()))
                .addHeader(
                    BERESTCommand.HEADER_APP_DISPLAY_VERSION,
                    ManifestInfo.getVersionName())
                .addHeader(BERESTCommand.HEADER_OS_VERSION, Build.VERSION.RELEASE)
                .addHeader(BERESTCommand.USER_AGENT, userAgent());

            // Only add the installationId if not already set
            if (request.getHeader(BERESTCommand.HEADER_INSTALLATION_ID) == null) {
              // We can do this synchronously since the caller is already in a Task on the
              // NETWORK_EXECUTOR
              builder.addHeader(BERESTCommand.HEADER_INSTALLATION_ID, installationId().get());
            }
            return chain.proceed(builder.build());
          }
        });
      }
      return restClient;
    }
  }

  // TODO(grantland): Pass through some system values.
  /* package */ String userAgent() {
    return "BE Java SDK";
  }

  /* package */ InstallationId installationId() {
    synchronized (lock) {
      if (installationId == null) {
        //noinspection deprecation
        installationId = new InstallationId(new File(getBEDir(), INSTALLATION_ID_LOCATION));
      }
      return installationId;
    }
  }

  @Deprecated
  /* package */ File getBEDir() {
    throw new IllegalStateException("Stub");
  }

  /* package */ File getCacheDir() {
    throw new IllegalStateException("Stub");
  }

  /* package */ File getFilesDir() {
    throw new IllegalStateException("Stub");
  }

  /* package */ static class Android extends BEPlugins {
    /* package */ static void initialize(Context context, String applicationId, String clientKey) {
      BEPlugins.set(new Android(context, applicationId, clientKey));
    }

    /* package */ static BEPlugins.Android get() {
      return (BEPlugins.Android) BEPlugins.get();
    }

    private final Context applicationContext;

    private Android(Context context, String applicationId, String clientKey) {
      super(applicationId, clientKey);
      applicationContext = context.getApplicationContext();
    }

    /* package */ Context applicationContext() {
      return applicationContext;
    }

    @Override
    public BEHttpClient newHttpClient() {
      SSLSessionCache sslSessionCache = new SSLSessionCache(applicationContext);
      int socketOperationTimeout = 10 * 1000; // 10 seconds
      return BEHttpClient.createClient(
          socketOperationTimeout,
          sslSessionCache);
    }

    @Override
    /* package */ String userAgent() {
      String packageVersion = "unknown";
      try {
        String packageName = applicationContext.getPackageName();
        int versionCode = applicationContext
            .getPackageManager()
            .getPackageInfo(packageName, 0)
            .versionCode;
        packageVersion = packageName + "/" + versionCode;
      } catch (PackageManager.NameNotFoundException e) {
        // Should never happen.
      }
      return "BE Android SDK " + BEObject.VERSION_NAME + " (" + packageVersion +
          ") API Level " + Build.VERSION.SDK_INT;
    }

    @Override
    @SuppressWarnings("deprecation")
    /* package */ File getBEDir() {
      synchronized (lock) {
        if (beDir == null) {
          beDir = applicationContext.getDir("CSBM", Context.MODE_PRIVATE);
        }
        return createFileDir(beDir);
      }
    }

    @Override
    /* package */ File getCacheDir() {
      synchronized (lock) {
        if (cacheDir == null) {
          cacheDir = new File(applicationContext.getCacheDir(), "com.csbm");
        }
        return createFileDir(cacheDir);
      }
    }

    @Override
    /* package */ File getFilesDir() {
      synchronized (lock) {
        if (filesDir == null) {
          filesDir = new File(applicationContext.getFilesDir(), "com.csbm");
        }
        return createFileDir(filesDir);
      }
    }
  }

  private static File createFileDir(File file) {
    if (!file.exists()) {
      if (!file.mkdirs()) {
        return file;
      }
    }
    return file;
  }
}
