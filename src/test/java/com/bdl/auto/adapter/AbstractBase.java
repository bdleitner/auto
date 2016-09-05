package com.bdl.auto.adapter;

/** An abstract base class to test out the annotation processor. */
public abstract class AbstractBase implements BaseInterface1, BaseInterface2 {

  @Override
  public String methodBase2(int input) {
    return String.valueOf(input);
  }

  @Override
  public String methodBase1(int input) {
    return String.valueOf(-input);
  }

  @Override
  public double methodBase1Super(String first, int second) {
    return (double) first.hashCode() / second;
  }
}
