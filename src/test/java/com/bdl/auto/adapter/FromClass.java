package com.bdl.auto.adapter;

import com.google.common.base.Strings;

/**
 * A test class to build an auto adapter.
 *
 * @author Ben Leitner
 */
@AutoAdapter
public abstract class FromClass extends AbstractBase implements Interface1 {

  @Override
  public void method1B() {
    // do nothing
  }

  @Override
  public String methodBase1(String first, int second) {
    int repeats = (second - 1) / first.length() + 1;
    return Strings.repeat(first, repeats).substring(0, second);
  }
}
