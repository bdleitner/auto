package com.bdl.auto.adapter;

import com.google.auto.value.AutoValue;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 * Holder of metadata for a method ExecutableElement.
 *
 * @author Ben Leitner
 */
@AutoValue
abstract class MethodMetadata implements Comparable<MethodMetadata> {

  abstract Visibility visibility();
  abstract String name();
  abstract TypeMetadata type();
  abstract ImmutableList<ParameterMetadata> parameters();

  abstract Builder toBuilder();

  static Builder builder() {
    return new AutoValue_MethodMetadata.Builder();
  }

  static MethodMetadata fromMethod(ExecutableElement element) {
    Preconditions.checkArgument(element.getKind() == ElementKind.METHOD,
        "Element %s is not a method.", element);
    Builder metadata = builder()
        .setVisibility(Visibility.forElement(element))
        .setType(TypeMetadata.fromType(element.getReturnType()))
        .setName(element.getSimpleName().toString());

    for (VariableElement parameter : element.getParameters()) {
      metadata.addParameter(
          ParameterMetadata.of(
              TypeMetadata.fromType(parameter.asType()),
              parameter.getSimpleName().toString()));
    }

    return metadata.build();
  }

  @Override
  public int compareTo(MethodMetadata that) {
    return ComparisonChain.start()
        .compare(visibility().ordinal(), that.visibility().ordinal())
        .compare(name(), that.name())
        .compare(parameters().size(), that.parameters().size())
        .compare(parameters(), that.parameters(), ParameterMetadata.IMMUTABLE_LIST_COMPARATOR)
        .result();
  }

  @Override
  public String toString() {
    return String.format("%s%s %s(%s)",
        visibility().prefix(),
        type().nameBuilder()
            .addPackagePrefix()
            .addNestingPrefix()
            .addSimpleName()
            .addSimpleParams()
            .toString(),
        name(),
        Joiner.on(", ").join(parameters()));
  }

  @AutoValue.Builder
  public static abstract class Builder {
    abstract Builder setVisibility(Visibility visibility);
    abstract Builder setName(String name);
    abstract Builder setType(TypeMetadata Type);
    abstract ImmutableList.Builder<ParameterMetadata> parametersBuilder();

    Builder addParameter(ParameterMetadata parameter) {
      parametersBuilder().add(parameter);
      return this;
    }

    abstract MethodMetadata build();
  }
}
