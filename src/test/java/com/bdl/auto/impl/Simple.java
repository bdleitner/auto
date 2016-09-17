package com.bdl.auto.impl;

/** Simple interface for testing the annotation processor. */
public interface Simple extends SuperSimple {

  int add(int first, int second);

  String repeat(String template, int times);
}
