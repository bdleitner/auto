package com.bdl.auto.adapter;

/** Simple interface for testing the annotation processor. */
public interface ParameterizedInterface<T> {

  boolean method2(int input);

  T frozzle(T input);
}
