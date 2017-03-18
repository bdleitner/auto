package com.bdl.auto.delegate.processor;

import com.bdl.auto.delegate.AutoDelegate;

/**
 * TODO: JavaDoc this class.
 *
 * @author Ben Leitner
 */
@AutoDelegate
public abstract class Simple implements Inherited {

  protected final Inherited delegate;
  private final int other;

  protected Simple(Inherited delegate) {
    this(delegate, 0);
  }

  protected Simple(Inherited delegate, int other) {
    this.delegate = delegate;
    this.other = other;
  }

  @Override
  public boolean something() {
    return false;
  }
}
