package com.bdl.auto.adapter;

import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * Encapsulation of metadata from the inheritance of an abstract class or interface.
 *
 * @author Ben Leitner
 */
@AutoValue
abstract class InheritanceMetadata {

  private ImmutableList<MethodMetadata> orderedNeededMethods;

  /** The type parameters given in the {@code extends} or {@code implements} clause. */
  abstract ImmutableList<TypeMetadata> inheritanceParams();

  /** The type that is inherited from (the object of the {@code extends} or {@code implements} clause). */
  abstract TypeMetadata type();

  /** The inheritance metadatas for the types that this one inherits from. */
  abstract ImmutableList<InheritanceMetadata> inheritances();

  /** Methods that are declared abstractly in this type. */
  abstract ImmutableList<MethodMetadata> methods();

  ImmutableList<MethodMetadata> getOrderedNeededMethods() {
    if (orderedNeededMethods == null) {
      final Map<String, String> paramNamesMap = getParamNamesMap();
      Set<MethodMetadata> neededMethods = Sets.newHashSet();
      for (InheritanceMetadata inheritance : inheritances()) {
        neededMethods.addAll(Sets.newHashSet(
            Iterables.transform(
                inheritance.getOrderedNeededMethods(),
                new Function<MethodMetadata, MethodMetadata>() {
                  @Override
                  public MethodMetadata apply(MethodMetadata input) {
                    return input.convertTypeParameters(paramNamesMap);
                  }
                })));
      }

      for (MethodMetadata method : methods()) {
        method = method.convertTypeParameters(paramNamesMap);
        if (method.isAbstract()) {
          neededMethods.add(method);
        } else {
          neededMethods.remove(method);
        }
      }

      List<MethodMetadata> ordered = Lists.newArrayList(neededMethods);
      Collections.sort(ordered);
      orderedNeededMethods = ImmutableList.copyOf(ordered);
    }
    return orderedNeededMethods;
  }

  private Map<String, String> getParamNamesMap() {
    ImmutableMap.Builder<String, String> paramNamesMap = ImmutableMap.builder();
    int i = 0;
    for (TypeMetadata typeParam : inheritanceParams()) {
      paramNamesMap.put(type().params().get(i).name(), typeParam.name());
      i++;
    }
    return paramNamesMap.build();
  }

  static InheritanceMetadata fromType(DeclaredType type) {
    Builder metadata = InheritanceMetadata.builder();
    for (TypeMirror typeParam : type.getTypeArguments()) {
      metadata.addInheritanceParam(TypeMetadata.fromType(typeParam));
    }
    Element element = type.asElement();
    metadata.setType(TypeMetadata.fromElement(element));

    for (TypeMirror inherited : ((TypeElement) element).getInterfaces()) {
      metadata.addInheritance(InheritanceMetadata.fromType((DeclaredType) inherited));
    }

    for (Element enclosed : element.getEnclosedElements()) {
      if (enclosed.getKind() == ElementKind.METHOD) {
        metadata.addMethod(MethodMetadata.fromMethod((ExecutableElement) enclosed));
      }
    }

    return metadata.build();
  }

  static InheritanceMetadata fromElement(Element element) {
    return fromType((DeclaredType) element.asType());
  }

  static Builder builder() {
    return new AutoValue_InheritanceMetadata.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract ImmutableList.Builder<TypeMetadata> inheritanceParamsBuilder();
    abstract Builder setType(TypeMetadata type);
    abstract ImmutableList.Builder<InheritanceMetadata> inheritancesBuilder();
    abstract ImmutableList.Builder<MethodMetadata> methodsBuilder();

    Builder addInheritanceParam(TypeMetadata type) {
      Preconditions.checkArgument(type.isTypeParameter() && type.bounds().isEmpty(),
          "Inheritance type params must be unbounded type parameters, was %s",
          type.fullDescription());
      inheritanceParamsBuilder().add(type);
      return this;
    }

    Builder addInheritance(InheritanceMetadata inheritance) {
      inheritancesBuilder().add(inheritance);
      return this;
    }

    Builder addMethod(MethodMetadata method) {
      methodsBuilder().add(method);
      return this;
    }

    abstract InheritanceMetadata autoBuild();

    InheritanceMetadata build() {
      InheritanceMetadata metadata = autoBuild();
      Preconditions.checkState(
          metadata.inheritanceParams().size() == metadata.type().params().size(),
          "Cannot inherit %s with type params <%s>, the sizes do not match.",
          metadata.type().fullDescription(),
          Joiner.on(", ").join(metadata.inheritanceParams()));

      return metadata;
    }
  }
}
