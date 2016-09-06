package com.bdl.auto.adapter;

import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.ElementKind;

/**
 * Metadata class for a relevant parts of a class to write.
 *
 * @author Ben Leitner
 */
@AutoValue
abstract class ClassMetadata {

  enum Category {
    CLASS,
    INTERFACE;

    static Category forKind(ElementKind kind) {
      switch (kind) {
        case CLASS:
          return Category.CLASS;
        case INTERFACE:
          return Category.INTERFACE;
        default:
          throw new IllegalArgumentException("Bad Kind: " + kind);
      }
    }
  }

  private ImmutableList<ConstructorMetadata> orderedRequiredConstructors;
  private ImmutableList<MethodMetadata> orderedRequiredMethods;

  abstract String packageName();

  abstract ImmutableList<String> containingClasses();

  abstract Category category();

  abstract String name();

  abstract ImmutableList<TypeParameterMetadata> typeParameters();

  abstract ImmutableSet<ConstructorMetadata> constructors();

  abstract ImmutableSet<MethodMetadata> abstractMethods();

  abstract ImmutableSet<MethodMetadata> implementedMethods();

  private String nesting(String delimiter) {
    return containingClasses().isEmpty()
        ? ""
        : Joiner.on(delimiter).join(Lists.reverse(containingClasses())) + delimiter;
  }

  String nestedClassName() {
    return nesting(".") + name();
  }

  String fullyQualifiedPathName() {
    return String.format("%s.%s%s", packageName(), nesting("."), name());
  }

  String decoratedName(String suffix) {
    return String.format("AutoAdapter_%s%s_%s", nesting("_"), name(), suffix);
  }

  String fullTypeParams() {
    return typeParameters().isEmpty()
        ? ""
        : String.format("<%s>", Joiner.on(", ").join(typeParameters()));
  }

  String unboundedTypeParams() {
    return typeParameters().isEmpty()
        ? ""
        : String.format("<%s>", Joiner.on(", ").join(
        Iterables.transform(
            typeParameters(),
            new Function<TypeParameterMetadata, String>() {
              @Override
              public String apply(TypeParameterMetadata input) {
                return input.name();
              }
            })));
  }

  ImmutableList<ConstructorMetadata> orderedRequiredConstructors() {
    if (orderedRequiredConstructors == null) {
      List<ConstructorMetadata> constructors = Lists.newArrayList(Iterables.filter(constructors(),
          new Predicate<ConstructorMetadata>() {
            @Override
            public boolean apply(ConstructorMetadata input) {
              return input.visibility() != Visibility.PRIVATE;
            }
          }));
      Collections.sort(constructors);
      // If there is only one constructor and has no parameters, then we don't need it.
      if (constructors.size() == 1 && constructors.get(0).parameters().size() == 0) {
        orderedRequiredConstructors = ImmutableList.of();
      } else {
        orderedRequiredConstructors = ImmutableList.copyOf(constructors);
      }
    }
    return orderedRequiredConstructors;
  }

  ImmutableList<MethodMetadata> orderedRequiredMethods() {
    if (orderedRequiredMethods == null) {
      ArrayList<MethodMetadata> methods = Lists.newArrayList(
          Sets.difference(abstractMethods(), implementedMethods()));
      Collections.sort(methods);
      orderedRequiredMethods = ImmutableList.copyOf(methods);
    }
    return orderedRequiredMethods;
  }

  @Override
  public String toString() {
    return fullyQualifiedPathName();
  }

  static Builder builder() {
    return new AutoValue_ClassMetadata.Builder();
  }

  @AutoValue.Builder
  static abstract class Builder {
    abstract Builder packageName(String packageName);

    abstract ImmutableList.Builder<String> containingClassesBuilder();

    abstract Builder category(Category category);

    abstract Builder name(String name);

    abstract ImmutableList.Builder<TypeParameterMetadata> typeParametersBuilder();

    abstract ImmutableSet.Builder<ConstructorMetadata> constructorsBuilder();

    abstract ImmutableSet.Builder<MethodMetadata> abstractMethodsBuilder();

    abstract ImmutableSet.Builder<MethodMetadata> implementedMethodsBuilder();

    Builder nestInside(String className) {
      containingClassesBuilder().add(className);
      return this;
    }

    Builder addTypeParameter(TypeParameterMetadata param) {
      typeParametersBuilder().add(param);
      return this;
    }

    Builder addConstructor(ConstructorMetadata constructor) {
      constructorsBuilder().add(constructor);
      return this;
    }

    Builder addAbstractMethod(MethodMetadata method) {
      abstractMethodsBuilder().add(method);
      return this;
    }

    Builder addImplementedMethod(MethodMetadata method) {
      implementedMethodsBuilder().add(method);
      return this;
    }

    abstract ClassMetadata build();
  }
}
