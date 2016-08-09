package com.csbm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Associates a class name for a subclass of BEObject.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface BEClassName {
  /**
   * @return The CSBM class name associated with the BEObject subclass.
   */
  String value();
}
