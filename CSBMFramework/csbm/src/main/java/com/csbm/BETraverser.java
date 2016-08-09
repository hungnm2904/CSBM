package com.csbm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Subclass BETraverser to make an function to be run recursively on every object pointed to on
 * the given object.
 */

/** package */ abstract class BETraverser {
  // Whether to recurse into BEObjects that are seen.
  private boolean traverseBEObjects;

  // Whether to call visit with the object passed in.
  private boolean yieldRoot;

  /**
   * Creates a new BETraverser.
   */
  public BETraverser() {
    traverseBEObjects = false;
    yieldRoot = false;
  }

  /**
   * Override this method to implement your own functionality.
   * @return true if you want the Traverser to continue. false if you want it to cancel.
   */
  protected abstract boolean visit(Object object);

  /**
   * Internal implementation of traverse.
   */
  private void traverseInternal(Object root, boolean yieldRoot, IdentityHashMap<Object, Object> seen) {
    if (root == null || seen.containsKey(root)) {
      return;
    }

    if (yieldRoot) {
      if (!visit(root)) {
        return;
      }
    }

    seen.put(root, root);

    if (root instanceof JSONObject) {
      JSONObject json = (JSONObject) root;
      Iterator<String> keys = json.keys();
      while (keys.hasNext()) {
        String key = keys.next();
        try {
          traverseInternal(json.get(key), true, seen);
        } catch (JSONException e) {
          // This should never happen.
          throw new RuntimeException(e);
        }
      }

    } else if (root instanceof JSONArray) {
      JSONArray array = (JSONArray) root;
      for (int i = 0; i < array.length(); ++i) {
        try {
          traverseInternal(array.get(i), true, seen);
        } catch (JSONException e) {
          // This should never happen.
          throw new RuntimeException(e);
        }
      }

    } else if (root instanceof Map) {
      Map<?, ?> map = (Map<?, ?>) root;
      for (Object value : map.values()) {
        traverseInternal(value, true, seen);
      }

    } else if (root instanceof List) {
      List<?> list = (List<?>) root;
      for (Object value : list) {
        traverseInternal(value, true, seen);
      }

    } else if (root instanceof BEObject) {
      if (traverseBEObjects) {
        BEObject object = (BEObject) root;
        for (String key : object.keySet()) {
          traverseInternal(object.get(key), true, seen);
        }
      }

    } else if (root instanceof BEACL) {
      BEACL acl = (BEACL) root;
      BEUser user = acl.getUnresolvedUser();
      if (user != null && user.isCurrentUser()) {
        traverseInternal(user, true, seen);
      }
    }
  }

  /**
   * Sets whether to recurse into BEObjects that are seen.
   * @return this to enable chaining.
   */
  public BETraverser setTraverseBEObjects(boolean newValue) {
    traverseBEObjects = newValue;
    return this;
  }

  /**
   * Sets whether to call visit with the object passed in.
   * @return this to enable chaining.
   */
  public BETraverser setYieldRoot(boolean newValue) {
    yieldRoot = newValue;
    return this;
  }

  /**
   * Causes the traverser to traverse all objects pointed to by root, recursively.
   */
  public void traverse(Object root) {
    IdentityHashMap<Object, Object> seen = new IdentityHashMap<Object, Object>();
    traverseInternal(root, yieldRoot, seen);
  }
}
