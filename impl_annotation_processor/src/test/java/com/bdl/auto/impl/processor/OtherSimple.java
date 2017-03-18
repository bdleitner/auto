package com.bdl.auto.impl.processor;

/**
 * @author Ben Leitner
 */
interface OtherSimple {

  String blorp(String input);

  @Override
  String toString();

  void doNothing(String input);
}
