package com.bdl.auto.adapter;

import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 * Encapsulation of constructor metadata.
 *
 * @author Ben Leitner
 */
@AutoValue
abstract class ConstructorMetadata implements Comparable<ConstructorMetadata> {

  abstract Visibility visibility();
  abstract ImmutableList<ParameterMetadata> parameters();

  @Override
  public int compareTo(ConstructorMetadata that) {
    return ComparisonChain.start()
        .compare(visibility().ordinal(), that.visibility().ordinal())
        .compare(parameters(), that.parameters(), ParameterMetadata.IMMUTABLE_LIST_COMPARATOR)
        .result();
  }

  public String toString(String className) {
    return String.format("%s%s(%s)", visibility().prefix(), className, Joiner.on(", ").join(parameters()));
  }

  public String superCall() {
    return String.format("super(%s)", Joiner.on(", ").join(Iterables.transform(parameters(),
        new Function<ParameterMetadata, String>() {
          @Override
          public String apply(ParameterMetadata input) {
            return input.name();
          }
        })));
  }

  @Override
  public String toString() {
    return toString("Constructor");
  }

  static ConstructorMetadata fromConstructor(Element element) {
    Preconditions.checkArgument(element.getKind() == ElementKind.CONSTRUCTOR,
        "Element %s is not a constructor.", element);

    ExecutableElement executable = (ExecutableElement) element;
    Builder constructor = builder()
        .visibility(Visibility.forElement(element));

    for (VariableElement parameter : executable.getParameters()) {
      constructor.addParameter(
          ParameterMetadata.of(
              parameter.asType().toString(),
              parameter.getSimpleName().toString()));
    }

    return constructor.build();
  }

  static Builder builder() {
    return new AutoValue_ConstructorMetadata.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder visibility(Visibility visibility);
    abstract ImmutableList.Builder<ParameterMetadata> parametersBuilder();

    Builder addParameter(ParameterMetadata parameter) {
      parametersBuilder().add(parameter);
      return this;
    }

    abstract ConstructorMetadata build();
  }
}
