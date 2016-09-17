package com.bdl.auto.impl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method should have a specified implementation type, regardless of the
 * settings on the parent class.
 *
 * @author Ben Leitner
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface MethodImpl {

  /** The implementation to use for this method.  Ignored if set to {@link ImplOption#USE_PARENT}. */
  ImplOption value() default ImplOption.USE_PARENT;
}
