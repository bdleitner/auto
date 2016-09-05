package com.bdl.auto.adapter;

/**
 * Another target for the interceptor.
 *
 * @author Ben Leitner
 */
@AutoAdapter
public abstract class FromInterfaces<T> implements Interface1, Interface2<T> {

  @Override
  public void method1B() {
    // no nothing.
  }
}
