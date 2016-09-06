package com.bdl.auto.adapter;

/**
 * A parameterized interface that extends another parameterized interface with a different type param.
 *
 * @author Ben Leitner
 */
@AutoAdapter
public interface ExtendedParameterized<S> extends Parameterized<S> {

  S extendedFrozzle(S input);
}
