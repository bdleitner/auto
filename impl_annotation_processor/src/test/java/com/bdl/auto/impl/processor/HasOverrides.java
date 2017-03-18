package com.bdl.auto.impl.processor;

import com.bdl.auto.impl.AutoImpl;
import com.bdl.auto.impl.ImplOption;
import com.bdl.auto.impl.MethodImpl;

/**
 * An interface that uses implementation overrides.
 *
 * @author Ben Leitner
 */
@AutoImpl(value = ImplOption.THROW_EXCEPTION,
    booleanImpl = ImplOption.RETURN_DEFAULT_VALUE,
    numericImpl = ImplOption.RETURN_DEFAULT_VALUE,
    stringImpl = ImplOption.USE_PARENT
)
@SuppressWarnings("unused") // Used via compile elements in tests.
interface HasOverrides {

  int intMethod();

  double doubleMethod();

  long longMethod();

  boolean booleanMethod();

  @MethodImpl(ImplOption.THROW_EXCEPTION)
  boolean overriddenBooleanMethod();

  String stringMethod();

  @MethodImpl(ImplOption.RETURN_DEFAULT_VALUE)
  String overriddenStringMethod();

  void voidMethod();

  @MethodImpl(ImplOption.RETURN_DEFAULT_VALUE)
  void overriddenVoidMethod();

  Object objectMethod();
}
