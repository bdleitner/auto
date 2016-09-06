package com.bdl.auto.adapter;

/**
 * A test class to build an auto adapter.
 *
 * @author Ben Leitner
 */
@AutoAdapter
public abstract class FromClass extends AbstractBase implements Simple {

  @Override
  public void method1B() {
    // do nothing
  }

  @Override
  public int methodBase1(String first, int second) {
    return first.length() + second;
  }
}
