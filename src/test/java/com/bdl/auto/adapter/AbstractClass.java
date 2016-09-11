package com.bdl.auto.adapter;

import java.util.List;

/**
 * A testing class that utilizes multiple forms of inheritance for testing.
 *
 * @author Ben Leitner
 */
@AutoAdapter
abstract class AbstractClass<A, B extends Comparable<B>, C extends List<B>>
    extends AbstractSuperclass<B>
    implements ExtendedExtendedParameterized<A>, ComplexParameterized<A, B, C> {

  private AbstractClass(String blargh) {
    super(blargh);
  }

  AbstractClass(int foo) {
    super(String.valueOf(foo));
  }

  public AbstractClass(boolean foo) {
    this(foo ? 1 : 0);
  }

  @Override
  public A frozzle(A input) {
    return input;
  }
}
