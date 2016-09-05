package com.bdl.auto.adapter;

/**
 * Another target for the interceptor.
 *
 * @author Ben Leitner
 */
@AutoAdapter
public abstract class FromInterfaces implements Interface1, Interface2 {

  @Override
  public void method1B() {
    // no nothing.
  }
}
