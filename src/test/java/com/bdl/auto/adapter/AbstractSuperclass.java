package com.bdl.auto.adapter;

import com.google.common.base.Strings;

/**
 * Abstract superclass for testing.
 *
 * @author Ben Leitner
 */
abstract class AbstractSuperclass<Q> implements Simple, OtherParameterized<Q> {

  protected final String aString;

  protected AbstractSuperclass(String aString) {
    this.aString = aString;
  }

  @Override
  public String repeat(String template, int times) {
    return Strings.repeat(template, times);
  }

  @Override
  public Q frumple(Q input) {
    return input;
  }

  public String blorp(String input) {
    return input;
  }
}
