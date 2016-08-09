package com.csbm;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

/** package */ class BECorePlugins {

  private static final BECorePlugins INSTANCE = new BECorePlugins();
  public static BECorePlugins getInstance() {
    return INSTANCE;
  }

  /* package */ static final String FILENAME_CURRENT_USER = "currentUser";
  /* package */ static final String PIN_CURRENT_USER = "_currentUser";
  /* package */ static final String FILENAME_CURRENT_INSTALLATION = "currentInstallation";
  /* package */ static final String PIN_CURRENT_INSTALLATION = "_currentInstallation";
  /* package */ static final String FILENAME_CURRENT_CONFIG = "currentConfig";

  private AtomicReference<BEObjectController> objectController = new AtomicReference<>();
  private AtomicReference<BEUserController> userController = new AtomicReference<>();
  private AtomicReference<BESessionController> sessionController = new AtomicReference<>();

  // TODO(mengyan): Inject into BEUserInstanceController
  private AtomicReference<BECurrentUserController> currentUserController =
    new AtomicReference<>();
  // TODO(mengyan): Inject into BEInstallationInstanceController
  private AtomicReference<BECurrentInstallationController> currentInstallationController =
      new AtomicReference<>();

  private AtomicReference<BEAuthenticationManager> authenticationController =
      new AtomicReference<>();

  private AtomicReference<BEQueryController> queryController = new AtomicReference<>();
  private AtomicReference<BEFileController> fileController = new AtomicReference<>();
  private AtomicReference<BEAnalyticsController> analyticsController = new AtomicReference<>();
  private AtomicReference<BECloudCodeController> cloudCodeController = new AtomicReference<>();
  private AtomicReference<BEConfigController> configController = new AtomicReference<>();
  private AtomicReference<BEPushController> pushController = new AtomicReference<>();
  private AtomicReference<BEPushChannelsController> pushChannelsController =
      new AtomicReference<>();
  private AtomicReference<BEDefaultACLController> defaultACLController = new AtomicReference<>();

  private AtomicReference<LocalIdManager> localIdManager = new AtomicReference<>();
  private AtomicReference<BEObjectSubclassingController> subclassingController = new AtomicReference<>();

  private BECorePlugins() {
    // do nothing
  }

  /* package for tests */ void reset() {
    objectController.set(null);
    userController.set(null);
    sessionController.set(null);

    currentUserController.set(null);
    currentInstallationController.set(null);

    authenticationController.set(null);

    queryController.set(null);
    fileController.set(null);
    analyticsController.set(null);
    cloudCodeController.set(null);
    configController.set(null);
    pushController.set(null);
    pushChannelsController.set(null);
    defaultACLController.set(null);

    localIdManager.set(null);
  }

  public BEObjectController getObjectController() {
    if (objectController.get() == null) {
      // TODO(grantland): Do not rely on CSBM global
      objectController.compareAndSet(
          null, new NetworkObjectController(BEPlugins.get().restClient()));
    }
    return objectController.get();
  }

  public void registerObjectController(BEObjectController controller) {
    if (!objectController.compareAndSet(null, controller)) {
      throw new IllegalStateException(
          "Another object controller was already registered: " + objectController.get());
    }
  }

  public BEUserController getUserController() {
    if (userController.get() == null) {
      // TODO(grantland): Do not rely on CSBM global
      userController.compareAndSet(
          null, new NetworkUserController(BEPlugins.get().restClient()));
    }
    return userController.get();
  }

  public void registerUserController(BEUserController controller) {
    if (!userController.compareAndSet(null, controller)) {
      throw new IllegalStateException(
          "Another user controller was already registered: " + userController.get());
    }
  }

  public BESessionController getSessionController() {
    if (sessionController.get() == null) {
      // TODO(grantland): Do not rely on CSBM global
      sessionController.compareAndSet(
          null, new NetworkSessionController(BEPlugins.get().restClient()));
    }
    return sessionController.get();
  }

  public void registerSessionController(BESessionController controller) {
    if (!sessionController.compareAndSet(null, controller)) {
      throw new IllegalStateException(
          "Another session controller was already registered: " + sessionController.get());
    }
  }

  public BECurrentUserController getCurrentUserController() {
    if (currentUserController.get() == null) {
      File file = new File(CSBM.getBEDir(), FILENAME_CURRENT_USER);
      FileObjectStore<BEUser> fileStore =
          new FileObjectStore<>(BEUser.class, file, BEUserCurrentCoder.get());
      BEObjectStore<BEUser> store = CSBM.isLocalDatastoreEnabled()
          ? new OfflineObjectStore<>(BEUser.class, PIN_CURRENT_USER, fileStore)
          : fileStore;
      BECurrentUserController controller = new CachedCurrentUserController(store);
      currentUserController.compareAndSet(null, controller);
    }
    return currentUserController.get();
  }

  public void registerCurrentUserController(BECurrentUserController controller) {
    if (!currentUserController.compareAndSet(null, controller)) {
      throw new IllegalStateException(
          "Another currentUser controller was already registered: " +
              currentUserController.get());
    }
  }

  public BEQueryController getQueryController() {
    if (queryController.get() == null) {
      NetworkQueryController networkController = new NetworkQueryController(
          BEPlugins.get().restClient());
      BEQueryController controller;
      // TODO(grantland): Do not rely on CSBM global
      if (CSBM.isLocalDatastoreEnabled()) {
        controller = new OfflineQueryController(
            CSBM.getLocalDatastore(),
            networkController);
      } else {
        controller = new CacheQueryController(networkController);
      }
      queryController.compareAndSet(null, controller);
    }
    return queryController.get();
  }

  public void registerQueryController(BEQueryController controller) {
    if (!queryController.compareAndSet(null, controller)) {
      throw new IllegalStateException(
          "Another query controller was already registered: " + queryController.get());
    }
  }

  public BEFileController getFileController() {
    if (fileController.get() == null) {
      // TODO(grantland): Do not rely on CSBM global
      fileController.compareAndSet(null, new BEFileController(
          BEPlugins.get().restClient(),
          CSBM.getBECacheDir("files")));
    }
    return fileController.get();
  }

  public void registerFileController(BEFileController controller) {
    if (!fileController.compareAndSet(null, controller)) {
      throw new IllegalStateException(
          "Another file controller was already registered: " + fileController.get());
    }
  }

  public BEAnalyticsController getAnalyticsController() {
    if (analyticsController.get() == null) {
      // TODO(mengyan): Do not rely on CSBM global
      analyticsController.compareAndSet(null,
          new BEAnalyticsController(CSBM.getEventuallyQueue()));
    }
    return analyticsController.get();
  }

  public void registerAnalyticsController(BEAnalyticsController controller) {
    if (!analyticsController.compareAndSet(null, controller)) {
      throw new IllegalStateException(
          "Another analytics controller was already registered: " + analyticsController.get());
    }
  }

  public BECloudCodeController getCloudCodeController() {
    if (cloudCodeController.get() == null) {
      cloudCodeController.compareAndSet(null, new BECloudCodeController(
          BEPlugins.get().restClient()));
    }
    return cloudCodeController.get();
  }

  public void registerCloudCodeController(BECloudCodeController controller) {
    if (!cloudCodeController.compareAndSet(null, controller)) {
      throw new IllegalStateException(
          "Another cloud code controller was already registered: " + cloudCodeController.get());
    }
  }

  public BEConfigController getConfigController() {
    if (configController.get() == null) {
      // TODO(mengyan): Do not rely on CSBM global
      File file = new File(BEPlugins.get().getBEDir(), FILENAME_CURRENT_CONFIG);
      BECurrentConfigController currentConfigController =
          new BECurrentConfigController(file);
      configController.compareAndSet(null, new BEConfigController(
          BEPlugins.get().restClient(), currentConfigController));
    }
    return configController.get();
  }

  public void registerConfigController(BEConfigController controller) {
    if (!configController.compareAndSet(null, controller)) {
      throw new IllegalStateException(
          "Another config controller was already registered: " + configController.get());
    }
  }

  public BEPushController getPushController() {
    if (pushController.get() == null) {
      pushController.compareAndSet(null, new BEPushController(BEPlugins.get().restClient()));
    }
    return pushController.get();
  }

  public void registerPushController(BEPushController controller) {
    if (!pushController.compareAndSet(null, controller)) {
      throw new IllegalStateException(
          "Another push controller was already registered: " + pushController.get());
    }
  }

  public BEPushChannelsController getPushChannelsController() {
    if (pushChannelsController.get() == null) {
      pushChannelsController.compareAndSet(null, new BEPushChannelsController());
    }
    return pushChannelsController.get();
  }

  public void registerPushChannelsController(BEPushChannelsController controller) {
    if (!pushChannelsController.compareAndSet(null, controller)) {
      throw new IllegalStateException(
          "Another pushChannels controller was already registered: " +
              pushChannelsController.get());
    }
  }

  public BECurrentInstallationController getCurrentInstallationController() {
    if (currentInstallationController.get() == null) {
      File file = new File(BEPlugins.get().getBEDir(), FILENAME_CURRENT_INSTALLATION);
      FileObjectStore<BEInstallation> fileStore =
          new FileObjectStore<>(BEInstallation.class, file, BEObjectCurrentCoder.get());
      BEObjectStore<BEInstallation> store = CSBM.isLocalDatastoreEnabled()
          ? new OfflineObjectStore<>(BEInstallation.class, PIN_CURRENT_INSTALLATION, fileStore)
          : fileStore;
      CachedCurrentInstallationController controller =
          new CachedCurrentInstallationController(store, BEPlugins.get().installationId());
      currentInstallationController.compareAndSet(null, controller);
    }
    return currentInstallationController.get();
  }

  public void registerCurrentInstallationController(BECurrentInstallationController controller) {
    if (!currentInstallationController.compareAndSet(null, controller)) {
      throw new IllegalStateException(
          "Another currentInstallation controller was already registered: " +
              currentInstallationController.get());
    }
  }

  public BEAuthenticationManager getAuthenticationManager() {
    if (authenticationController.get() == null) {
      BEAuthenticationManager controller =
          new BEAuthenticationManager(getCurrentUserController());
      authenticationController.compareAndSet(null, controller);
    }
    return authenticationController.get();
  }

  public void registerAuthenticationManager(BEAuthenticationManager manager) {
    if (!authenticationController.compareAndSet(null, manager)) {
      throw new IllegalStateException(
          "Another authentication manager was already registered: " +
              authenticationController.get());
    }
  }

  public BEDefaultACLController getDefaultACLController() {
    if (defaultACLController.get() == null) {
      BEDefaultACLController controller = new BEDefaultACLController();
      defaultACLController.compareAndSet(null, controller);
    }
    return defaultACLController.get();
  }

  public void registerDefaultACLController(BEDefaultACLController controller) {
    if (!defaultACLController.compareAndSet(null, controller)) {
      throw new IllegalStateException(
          "Another defaultACL controller was already registered: " + defaultACLController.get());
    }
  }

  public LocalIdManager getLocalIdManager() {
    if (localIdManager.get() == null) {
      LocalIdManager manager = new LocalIdManager(CSBM.getBEDir());
      localIdManager.compareAndSet(null, manager);
    }
    return localIdManager.get();
  }

  public void registerLocalIdManager(LocalIdManager manager) {
    if (!localIdManager.compareAndSet(null, manager)) {
      throw new IllegalStateException(
          "Another localId manager was already registered: " + localIdManager.get());
    }
  }

  public BEObjectSubclassingController getSubclassingController() {
    if (subclassingController.get() == null) {
      BEObjectSubclassingController controller = new BEObjectSubclassingController();
      subclassingController.compareAndSet(null, controller);
    }
    return subclassingController.get();
  }

  public void registerSubclassingController(BEObjectSubclassingController controller) {
    if (!subclassingController.compareAndSet(null, controller)) {
      throw new IllegalStateException(
          "Another subclassing controller was already registered: " + subclassingController.get());
    }
  }
}

