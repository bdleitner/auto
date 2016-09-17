package com.bdl.auto.impl;

/**
 * A parameterized interface that extends another parameterized interface with a different type param.
 *
 * @author Ben Leitner
 */
@AutoImpl
public interface ExtendedParameterized<S> extends Parameterized<S> {

  S extendedFrozzle(S input);
}
