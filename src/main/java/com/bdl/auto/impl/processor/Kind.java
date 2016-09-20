package com.bdl.auto.impl.processor;

/**
 * Enumeration of the broad kinds of types.
 *
 * @author Ben Leitner
 */
enum Kind {
  /** primitive or boxed numeric types. */
  NUMERIC,
  /** primitive or boxed booleans. */
  BOOLEAN,
  /** voids. */
  VOID,
  /** Strings. */
  STRING,
  /** All other objects. */
  OBJECT
}
