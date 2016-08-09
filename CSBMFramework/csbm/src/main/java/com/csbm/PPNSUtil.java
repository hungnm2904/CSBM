package com.csbm;

import android.app.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/** package */ class PPNSUtil {
  /* package for tests */ static String CLASS_PPNS_SERVICE = "com.csbm.PPNSService";

  public static boolean isPPNSAvailable() {
    try {
      Class.forName(CLASS_PPNS_SERVICE);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  @SuppressWarnings("TryWithIdenticalCatches")
  public static ProxyService newPPNSService(Service service) {
    try {
      Class<?> clazz = Class.forName(CLASS_PPNS_SERVICE);
      Constructor<?> cons = clazz.getDeclaredConstructor(Service.class);
      return (ProxyService) cons.newInstance(service);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
