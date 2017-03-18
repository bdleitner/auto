package com.bdl.auto.impl;

/**
 * Enumeration of possible implementations for a method.
 *
 * @author Ben Leitner
 */
public enum ImplOption {
  /**
   * Return a default value ({@code 0} for primitive numeric types, {@code ""} for strings, {@code false} for
   * booleans and {@code null} for other objects.
   */
  RETURN_DEFAULT_VALUE,
  /** Throw an {@code UnsupportedOperationException}. */
  THROW_EXCEPTION,
  /** Defer to the parent class's annotation or its default to decide what to do. */
  USE_PARENT
}
