package com.bdl.auto.adapter;

import com.google.auto.value.AutoValue;

/**
 * Data object for parameters.
 *
 * @author Ben Leitner
 */
@AutoValue
abstract class ParameterMetadata {
  abstract String type();
  abstract String name();

  static ParameterMetadata of(String type, String name) {
    return new AutoValue_ParameterMetadata(type, name);
  }

  @Override
  public String toString() {
    return String.format("%s %s", type(), name());
  }
}
