package com.bdl.auto.adapter;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ComparisonChain;
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
      = new Comparator<ImmutableList<ParameterMetadata>>() {
    @Override
    public int compare(ImmutableList<ParameterMetadata> o1, ImmutableList<ParameterMetadata> o2) {
      ComparisonChain chain = ComparisonChain.start();
      chain = chain.compare(o1.size(), o2.size());
      for (int i = 0; i < Math.min(o1.size(), o2.size()); i++) {
        chain = chain.compare(o1.get(i).type(), o2.get(i).type());
      }
      return chain.result();
    }
  };

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
