package com.bdl.auto.adapter;

/**
 * A parameterized interface that extends another parameterized interface that extends another parameterized interface,
 * al with different type params.
 *
 * @author Ben Leitner
 */
public interface ExtendedExtendedParameterized<C> extends ExtendedParameterized<C> {

  C superExtendedFrozzle(C input);
}
