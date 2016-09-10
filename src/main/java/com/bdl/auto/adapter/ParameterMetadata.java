package com.bdl.auto.adapter;

import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import java.util.Comparator;

/**
 * Data object for parameters.
 *
 * @author Ben Leitner
 */
@AutoValue
abstract class ParameterMetadata {
  static final Comparator<ImmutableList<ParameterMetadata>> IMMUTABLE_LIST_COMPARATOR
      = Comparators.forLists(new Function<ParameterMetadata, TypeMetadata>() {
    @Override
    public TypeMetadata apply(ParameterMetadata input) {
      return input.type();
    }
  });

  abstract TypeMetadata type();
  abstract String name();

  static ParameterMetadata of(TypeMetadata type, String name) {
    return new AutoValue_ParameterMetadata(type, name);
  }

  @Override
  public String toString() {
    return String.format("%s %s",
        type().nameBuilder()
            .addPackagePrefix()
            .addNestingPrefix()
            .addSimpleName()
            .addSimpleParams()
            .toString(),
        name());
  }
}
