/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.csbm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * An operation where a BERelation's value is modified.
 */

/** package */ class BERelationOperation<T extends BEObject> implements BEFieldOperation {
  // The className of the target objects.
  private final String targetClass;

  // A set of objects to add to this relation.
  private final Set<BEObject> relationsToAdd;
  // A set of objects to remove from this relation.
  private final Set<BEObject> relationsToRemove;

  BERelationOperation(Set<T> newRelationsToAdd, Set<T> newRelationsToRemove) {
    String targetClass = null;
    relationsToAdd = new HashSet<>();
    relationsToRemove = new HashSet<>();

    if (newRelationsToAdd != null) {
      for (T object : newRelationsToAdd) {
        addBEObjectToSet(object, relationsToAdd);

        if (targetClass == null) {
          targetClass = object.getClassName();
        } else {
          if (!targetClass.equals(object.getClassName())) {
            throw new IllegalArgumentException(
                "All objects in a relation must be of the same class.");
          }
        }
      }
    }

    if (newRelationsToRemove != null) {
      for (T object : newRelationsToRemove) {
        addBEObjectToSet(object, relationsToRemove);

        if (targetClass == null) {
          targetClass = object.getClassName();
        } else {
          if (!targetClass.equals(object.getClassName())) {
            throw new IllegalArgumentException(
                "All objects in a relation must be of the same class.");
          }
        }
      }
    }

    if (targetClass == null) {
      throw new IllegalArgumentException("Cannot create a BERelationOperation with no objects.");
    }
    this.targetClass = targetClass;
  }

  private BERelationOperation(String newTargetClass, Set<BEObject> newRelationsToAdd,
                              Set<BEObject> newRelationsToRemove) {
    targetClass = newTargetClass;
    relationsToAdd = new HashSet<>(newRelationsToAdd);
    relationsToRemove = new HashSet<>(newRelationsToRemove);
  }

  /*
   * Adds a BEObject to a set, replacing any existing instance of the same object.
   */
  private void addBEObjectToSet(BEObject obj, Set<BEObject> set) {
    if (CSBM.getLocalDatastore() != null || obj.getObjectId() == null) {
      // There's no way there could be duplicate instances.
      set.add(obj);
      return;
    }
    
    // We have to do this the hard way.
    for (BEObject existingObject : set) {
      if (obj.getObjectId().equals(existingObject.getObjectId())) {
        set.remove(existingObject);
      }
    }
    set.add(obj);
  }

  /*
   * Adds a list of BEObject to a set, replacing any existing instance of the same object.
   */
  private void addAllBEObjectsToSet(Collection<BEObject> list, Set<BEObject> set) {
    for (BEObject obj : list) {
      addBEObjectToSet(obj, set);
    }
  }
  
  /*
   * Removes an object (and any duplicate instances of that object) from the set.
   */
  private void removeBEObjectFromSet(BEObject obj, Set<BEObject> set) {
    if (CSBM.getLocalDatastore() != null || obj.getObjectId() == null) {
      // There's no way there could be duplicate instances.
      set.remove(obj);
      return;
    }
    
    // We have to do this the hard way.
    for (BEObject existingObject : set) {
      if (obj.getObjectId().equals(existingObject.getObjectId())) {
        set.remove(existingObject);
      }
    }
  }

  /*
   * Removes all objects (and any duplicate instances of those objects) from the set.
   */
  private void removeAllBEObjectsFromSet(Collection<BEObject> list, Set<BEObject> set) {
    for (BEObject obj : list) {
      removeBEObjectFromSet(obj, set);
    }
  }

  String getTargetClass() {
    return targetClass;
  }

  /*
   * Converts a set of objects into a JSONArray of BE pointers.
   */
  JSONArray convertSetToArray(Set<BEObject> set, BEEncoder objectEncoder)
      throws JSONException {
    JSONArray array = new JSONArray();
    for (BEObject obj : set) {
      array.put(objectEncoder.encode(obj));
    }
    return array;
  }

  // Encodes any add/removes ops to JSON to send to the server.
  @Override
  public JSONObject encode(BEEncoder objectEncoder) throws JSONException {
    JSONObject adds = null;
    JSONObject removes = null;

    if (relationsToAdd.size() > 0) {
      adds = new JSONObject();
      adds.put("__op", "AddRelation");
      adds.put("objects", convertSetToArray(relationsToAdd, objectEncoder));
    }

    if (relationsToRemove.size() > 0) {
      removes = new JSONObject();
      removes.put("__op", "RemoveRelation");
      removes.put("objects", convertSetToArray(relationsToRemove, objectEncoder));
    }

    if (adds != null && removes != null) {
      JSONObject result = new JSONObject();
      result.put("__op", "Batch");
      JSONArray ops = new JSONArray();
      ops.put(adds);
      ops.put(removes);
      result.put("ops", ops);
      return result;
    }

    if (adds != null) {
      return adds;
    }

    if (removes != null) {
      return removes;
    }

    throw new IllegalArgumentException("A BERelationOperation was created without any data.");
  }

  @Override
  public BEFieldOperation mergeWithPrevious(BEFieldOperation previous) {
    if (previous == null) {
      return this;

    } else if (previous instanceof BEDeleteOperation) {
      throw new IllegalArgumentException("You can't modify a relation after deleting it.");

    } else if (previous instanceof BERelationOperation) {
      @SuppressWarnings("unchecked")
      BERelationOperation<T> previousOperation = (BERelationOperation<T>) previous;

      if (previousOperation.targetClass != null
          && !previousOperation.targetClass.equals(targetClass)) {
        throw new IllegalArgumentException("Related object object must be of class "
            + previousOperation.targetClass + ", but " + targetClass + " was passed in.");
      }

      Set<BEObject> newRelationsToAdd = new HashSet<>(previousOperation.relationsToAdd);
      Set<BEObject> newRelationsToRemove = new HashSet<>(previousOperation.relationsToRemove);
      if (relationsToAdd != null) {
        addAllBEObjectsToSet(relationsToAdd, newRelationsToAdd);
        removeAllBEObjectsFromSet(relationsToAdd, newRelationsToRemove);
      }
      if (relationsToRemove != null) {
        removeAllBEObjectsFromSet(relationsToRemove, newRelationsToAdd);
        addAllBEObjectsToSet(relationsToRemove, newRelationsToRemove);
      }
      return new BERelationOperation<T>(targetClass, newRelationsToAdd, newRelationsToRemove);

    } else {
      throw new IllegalArgumentException("Operation is invalid after previous operation.");
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object apply(Object oldValue, String key) {
    BERelation<T> relation;

    if (oldValue == null) {
      relation = new BERelation<>(targetClass);

    } else if (oldValue instanceof BERelation) {
      relation = (BERelation<T>) oldValue;
      if (targetClass != null && !targetClass.equals(relation.getTargetClass())) {
        throw new IllegalArgumentException("Related object object must be of class "
            + relation.getTargetClass() + ", but " + targetClass + " was passed in.");
      }
    } else {
      throw new IllegalArgumentException("Operation is invalid after previous operation.");
    }
    
    for (BEObject relationToAdd : relationsToAdd) {
      relation.addKnownObject(relationToAdd);
    }
    for (BEObject relationToRemove : relationsToRemove) {
      relation.removeKnownObject(relationToRemove);
    }
    return relation;
  }
}
