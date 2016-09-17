package com.bdl.auto.impl;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

/**
 * Encapsulation of Metadata information for an Annotation reference.
 *
 * @author Ben Leitner
 */
@AutoValue
abstract class AnnotationMetadata implements GeneratesImports {

  /** The type of the annotation. */
  abstract TypeMetadata type();

  abstract ImmutableMap<MethodMetadata, ValueMetadata> values();

  @Override
  public Set<String> getImports() {
    ImmutableSet.Builder<String> imports = ImmutableSet.builder();
    imports.addAll(type().getImports());
    for (Map.Entry<MethodMetadata, ValueMetadata> entry : values().entrySet()) {
      imports.addAll(entry.getKey().getImports());
      imports.addAll(entry.getValue().type().getImports());
    }
    return imports.build();
  }

  static AnnotationMetadata fromType(AnnotationMirror mirror) {
    Builder metadata = AnnotationMetadata.builder();
    metadata.setType(TypeMetadata.fromType(mirror.getAnnotationType()));
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
        : mirror.getElementValues().entrySet()) {
      metadata.putValue(
          MethodMetadata.fromMethod(entry.getKey()),
          valueFor(entry.getValue()));
    }
    return metadata.build();
  }

  private static ValueMetadata valueFor(AnnotationValue annotationValue) {
    Element value = (Element) annotationValue.getValue();
    TypeMetadata type = TypeMetadata.fromElement(value);
    return ValueMetadata.create(type, value.getSimpleName().toString());
  }

  static Builder builder() {
    return new AutoValue_AnnotationMetadata.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setType(TypeMetadata type);
    abstract ImmutableMap.Builder<MethodMetadata, ValueMetadata> valuesBuilder();

    Builder putValue(MethodMetadata metadata, ValueMetadata object) {
      valuesBuilder().put(metadata, object);
      return this;
    }

    abstract AnnotationMetadata build();
  }
}
