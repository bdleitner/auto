package com.bdl.auto.impl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class should have a concrete implementation class generated for it.
 *
 * @author Ben Leitner
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface AutoImpl {

  /**
   * The default implementation to use when implementing methods.
   * <p>
   * Cannot be {@link ImplOption#USE_PARENT}.
   */
  ImplOption value() default ImplOption.THROW_EXCEPTION;

  /**
   * The implementation to use for methods that return a numeric type. If {@code USE_PARENT}, defaults to
   * {@link #value()}.
   */
  ImplOption numericImpl() default ImplOption.USE_PARENT;

  /**
   * The implementation to use for methods that return a boolean. If {@code USE_PARENT}, defaults to
   * {@link #value()}.
   */
  ImplOption booleanImpl() default ImplOption.USE_PARENT;

  /**
   * The implementation to use for methods that do not return a value. If {@code USE_PARENT}, defaults to
   * {@link #value()}.
   */
  ImplOption voidImpl() default ImplOption.USE_PARENT;

  /**
   * The implementation to use for methods that return a String. If {@code USE_PARENT}, defaults to
   * {@link #value()}.
   */
  ImplOption stringImpl() default ImplOption.USE_PARENT;

  /**
   * The implementation to use for methods that return a non-String, non-boxed Object type. If {@code USE_PARENT},
   * defaults to {@link #value()}.
   */
  ImplOption objectImpl() default ImplOption.USE_PARENT;
}
