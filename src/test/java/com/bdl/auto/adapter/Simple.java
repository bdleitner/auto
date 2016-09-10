package com.bdl.auto.adapter;

/** Simple interface for testing the annotation processor. */
public interface Simple extends Interface1Super {

  int add(int first, int second);

  String repeat(String template, int times);

  int method1A();

  void method1B();
}
