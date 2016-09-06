package com.bdl.auto.adapter;

import com.google.auto.value.AutoValue;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

/**
 * Encapsulation of data for a type parameter.
 *
 * @author Ben Leitner
 */
@AutoValue
abstract class TypeParameterMetadata {

  abstract String name();
  abstract ImmutableList<String> bounds();

  @Override
  public String toString() {
    return bounds().isEmpty()
        ? name()
        : String.format("%s extends %s", name(), Joiner.on(" & ").join(bounds()));
  }

  static TypeParameterMetadata fromElement(Element element) {
    Preconditions.checkArgument(element.getKind() == ElementKind.TYPE_PARAMETER,
        "Element %s is not a type parameter.");
    Builder builder = builder().setName(element.getSimpleName().toString());

    for (TypeMirror type : ((TypeParameterElement) element).getBounds()) {
      if (type.toString().equals("java.lang.Object")) {
        continue;
      }
      builder.addBound(type.toString());
    }

    return builder.build();
  }

  static Builder builder() {
    return new AutoValue_TypeParameterMetadata.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setName(String name);
    abstract ImmutableList.Builder<String> boundsBuilder();

    Builder addBound(String bound) {
      boundsBuilder().add(bound);
      return this;
    }

    abstract TypeParameterMetadata build();
  }
}
