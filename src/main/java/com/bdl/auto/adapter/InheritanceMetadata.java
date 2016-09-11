package com.bdl.auto.adapter;

import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * Encapsulation of metadata from the inheritance of an abstract class or interface.
 *
 * @author Ben Leitner
 */
@AutoValue
abstract class InheritanceMetadata implements GeneratesMethods, GeneratesImports {

  private ImmutableList<MethodMetadata> orderedNeededMethods;

  /** The type parameters given in the {@code extends} or {@code implements} clause. */
  abstract ImmutableList<TypeMetadata> inheritanceParams();

  abstract ClassMetadata classMetadata();

  @Override
  public ImmutableList<String> getImports() {
    return classMetadata().getImports();
  }

  @Override
  public ImmutableList<MethodMetadata> getOrderedRequiredMethods() {
    if (orderedNeededMethods == null) {
      final Map<String, String> paramNamesMap = getParamNamesMap();
      orderedNeededMethods = ImmutableList.copyOf(
          Iterables.transform(
              classMetadata().getOrderedRequiredMethods(),
              new Function<MethodMetadata, MethodMetadata>() {
                  @Override
                  public MethodMetadata apply(MethodMetadata input) {
                    return input.convertTypeParameters(paramNamesMap);
                  }
                }));
    }
    return orderedNeededMethods;
  }

  private Map<String, String> getParamNamesMap() {
    ImmutableMap.Builder<String, String> paramNamesMap = ImmutableMap.builder();
    int i = 0;
    for (TypeMetadata typeParam : inheritanceParams()) {
      paramNamesMap.put(classMetadata().type().params().get(i).name(), typeParam.name());
      i++;
    }
    return paramNamesMap.build();
  }

  static InheritanceMetadata fromType(DeclaredType type) {
    Builder metadata = InheritanceMetadata.builder();
    for (TypeMirror typeParam : type.getTypeArguments()) {
      metadata.addInheritanceParam(TypeMetadata.fromType(typeParam));
    }
    metadata.setClassMetadata(ClassMetadata.fromElement(type.asElement()));
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
    abstract Builder setClassMetadata(ClassMetadata classMetadata);

    Builder addInheritanceParam(TypeMetadata type) {
      Preconditions.checkArgument(type.isTypeParameter() && type.bounds().isEmpty(),
          "Inheritance type params must be unbounded type parameters, was %s",
          type.fullDescription());
      inheritanceParamsBuilder().add(type);
      return this;
    }

    abstract InheritanceMetadata autoBuild();

    InheritanceMetadata build() {
      InheritanceMetadata metadata = autoBuild();
      Preconditions.checkState(
          metadata.inheritanceParams().size() == metadata.classMetadata().type().params().size(),
          "Cannot inherit %s with type params <%s>, the sizes do not match.",
          metadata.classMetadata().type().fullDescription(),
          Joiner.on(", ").join(metadata.inheritanceParams()));

      return metadata;
    }
  }
}
