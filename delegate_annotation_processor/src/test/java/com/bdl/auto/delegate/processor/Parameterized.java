package com.bdl.auto.delegate.processor;

import com.bdl.auto.delegate.AutoDelegate;

/**
 * TODO: JavaDoc this class.
 *
 * @author Ben Leitner
 */
@AutoDelegate
public abstract class Parameterized implements ParameterizedInherited<Long> {

  protected final ParameterizedInherited<Long> delegate;

  protected Parameterized(ParameterizedInherited<Long> delegate) {
    this.delegate = delegate;
  }

  @Override
  public int bar(int baz) {
    return baz + 1;
  }
}
