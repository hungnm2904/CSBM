package com.csbm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/* package */ class BEObjectSubclassingController {
  private final Object mutex = new Object();
  private final Map<String, Constructor<? extends BEObject>> registeredSubclasses = new HashMap<>();

  /* package */ String getClassName(Class<? extends BEObject> clazz) {
    BEClassName info = clazz.getAnnotation(BEClassName.class);
    if (info == null) {
      throw new IllegalArgumentException("No BEClassName annotation provided on " + clazz);
    }
    return info.value();
  }

  /* package */ boolean isSubclassValid(String className, Class<? extends BEObject> clazz) {
    Constructor<? extends BEObject> constructor = null;

    synchronized (mutex) {
      constructor = registeredSubclasses.get(className);
    }

    return constructor == null
      ? clazz == BEObject.class
      : constructor.getDeclaringClass() == clazz;
  }

  /* package */ void registerSubclass(Class<? extends BEObject> clazz) {
    if (!BEObject.class.isAssignableFrom(clazz)) {
      throw new IllegalArgumentException("Cannot register a type that is not a subclass of BEObject");
    }

    String className = getClassName(clazz);
    Constructor<? extends BEObject> previousConstructor = null;

    synchronized (mutex) {
      previousConstructor = registeredSubclasses.get(className);
      if (previousConstructor != null) {
        Class<? extends BEObject> previousClass = previousConstructor.getDeclaringClass();
        if (clazz.isAssignableFrom(previousClass)) {
          // Previous subclass is more specific or equal to the current type, do nothing.
          return;
        } else if (previousClass.isAssignableFrom(clazz)) {
          // Previous subclass is parent of new child subclass, fallthrough and actually
          // register this class.
          /* Do nothing */
        } else {
          throw new IllegalArgumentException(
            "Tried to register both " + previousClass.getName() + " and " + clazz.getName() +
            " as the BEObject subclass of " + className + ". " + "Cannot determine the right " +
            "class to use because neither inherits from the other."
          );
        }
      }

      try {
        registeredSubclasses.put(className, getConstructor(clazz));
      } catch (NoSuchMethodException ex) {
        throw new IllegalArgumentException(
          "Cannot register a type that does not implement the default constructor!"
        );
      } catch (IllegalAccessException ex) {
        throw new IllegalArgumentException(
          "Default constructor for " + clazz + " is not accessible."
        );
      }
    }

    if (previousConstructor != null) {
      // TODO: This is super tightly coupled. Let's remove it when automatic registration is in.
      // NOTE: Perform this outside of the mutex, to prevent any potential deadlocks.
      if (className.equals(getClassName(BEUser.class))) {
        BEUser.getCurrentUserController().clearFromMemory();
      } else if (className.equals(getClassName(BEInstallation.class))) {
        BEInstallation.getCurrentInstallationController().clearFromMemory();
      }
    }
  }

  /* package */ void unregisterSubclass(Class<? extends BEObject> clazz) {
    String className = getClassName(clazz);

    synchronized (mutex) {
      registeredSubclasses.remove(className);
    }
  }

  /* package */ BEObject newInstance(String className) {
    Constructor<? extends BEObject> constructor = null;

    synchronized (mutex) {
      constructor = registeredSubclasses.get(className);
    }

    try {
      return constructor != null
        ? constructor.newInstance()
        : new BEObject(className);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Failed to create instance of subclass.", e);
    }
  }

  private static Constructor<? extends BEObject> getConstructor(Class<? extends  BEObject> clazz) throws NoSuchMethodException, IllegalAccessException {
    Constructor<? extends BEObject> constructor = clazz.getDeclaredConstructor();
    if (constructor == null) {
      throw new NoSuchMethodException();
    }
    int modifiers = constructor.getModifiers();
    if (Modifier.isPublic(modifiers) || (clazz.getPackage().getName().equals("com.csbm") &&
      !(Modifier.isProtected(modifiers) || Modifier.isPrivate(modifiers)))) {
      return constructor;
    }
    throw new IllegalAccessException();
  }
}
