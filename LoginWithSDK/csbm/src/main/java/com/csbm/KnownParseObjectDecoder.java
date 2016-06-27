package com.csbm;

import java.util.Map;

/**
 * A subclass of <code>BEDecoder</code> which can keep <code>BEObject</code> that
 * has been fetched instead of creating a new instance.
 */

/** package */ class KnownBEObjectDecoder extends BEDecoder {
  private Map<String, BEObject> fetchedObjects;

  public KnownBEObjectDecoder(Map<String, BEObject> fetchedObjects) {
    super();
    this.fetchedObjects = fetchedObjects;
  }

  /**
   * If the object has been fetched, the fetched object will be returned. Otherwise a
   * new created object will be returned.
   */
  @Override
  protected BEObject decodePointer(String className, String objectId) {
    if (fetchedObjects != null && fetchedObjects.containsKey(objectId)) {
      return fetchedObjects.get(objectId);
    }
    return super.decodePointer(className, objectId); 
  }
}
