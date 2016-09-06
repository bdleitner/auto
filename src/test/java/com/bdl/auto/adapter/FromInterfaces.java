package com.bdl.auto.adapter;

/**
 * Another target for the interceptor.
 *
 * @author Ben Leitner
 */
@AutoAdapter
public abstract class FromInterfaces<T> implements SimpleInterface, ParameterizedInterface<T> {

  @Override
  public void method1B() {
    // no nothing.
  }
}
