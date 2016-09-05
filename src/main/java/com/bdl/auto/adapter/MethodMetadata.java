package com.bdl.auto.adapter;

import com.google.auto.value.AutoValue;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

/**
 * Holder of metadata for a method ExecutableElement.
 *
 * @author Ben Leitner
 */
@AutoValue
abstract class MethodMetadata {

  enum Visibility {
    PUBLIC,
    PROTECTED,
    PACKAGE_LOCAL,
    PRIVATE
  }

  abstract Visibility visibility();
  abstract String name();
  abstract String type();
  abstract ImmutableList<ParameterMetadata> parameters();

  abstract Builder toBuilder();

  static Builder builder() {
    return new AutoValue_MethodMetadata.Builder();
  }

  static MethodMetadata fromMethod(ExecutableElement element) {
    Preconditions.checkArgument(element.getKind() == ElementKind.METHOD,
        "Element %s is not a method.", element);
    Builder metadata = builder()
        .setVisibility(getVisibility(element))
        .setType(element.getReturnType().toString())
        .setName(element.getSimpleName().toString());

    for (VariableElement parameter : element.getParameters()) {
      metadata.addParameter(
          ParameterMetadata.of(
              parameter.asType().toString(),
              parameter.getSimpleName().toString()));
    }

    return metadata.build();
  }

  private static Visibility getVisibility(ExecutableElement element) {
    Set<Modifier> modifiers = element.getModifiers();
    if (modifiers.contains(Modifier.PUBLIC)) {
      return Visibility.PUBLIC;
    }
    if (modifiers.contains(Modifier.PRIVATE)) {
      return Visibility.PRIVATE;
    }
    if (modifiers.contains(Modifier.PROTECTED)) {
      return Visibility.PROTECTED;
    }
    return Visibility.PACKAGE_LOCAL;
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    if (visibility() != Visibility.PACKAGE_LOCAL) {
      s.append(visibility().name().toLowerCase()).append(" ");
    }
    s.append(type()).append(" ");
    s.append(name()).append("(");
    s.append(Joiner.on(", ").join(parameters()));
    s.append(")");
    return s.toString();
  }

  @AutoValue.Builder
  public static abstract class Builder {
    abstract Builder setVisibility(Visibility visibility);
    abstract Builder setName(String name);
    abstract Builder setType(String Type);
    abstract ImmutableList.Builder<ParameterMetadata> parametersBuilder();

    Builder addParameter(ParameterMetadata parameter) {
      parametersBuilder().add(parameter);
      return this;
    }

    abstract MethodMetadata build();
  }
}
