package com.bdl.auto.impl.processor;

/** Simple interface for testing the annotation processor. */
public interface Parameterized<T> {

  T frozzle(T input);
}
