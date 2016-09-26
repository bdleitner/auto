package com.bdl.auto.impl.processor;

import com.bdl.auto.impl.AutoImpl;

import java.util.List;

/**
 * A testing class that utilizes multiple forms of inheritance for testing.
 *
 * @author Ben Leitner
 */
@AutoImpl
@SuppressWarnings("unused") // Used via elements in tests
abstract class AbstractClass<A, B extends Comparable<B>, C extends List<B>>
    extends AbstractSuperclass<B>
    implements ExtendedExtendedParameterized<A>, ComplexParameterized<A, B, C>, OtherSimple {

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

  @Override
  protected int fromSuper(int foo) {
    return foo;
  }
}
