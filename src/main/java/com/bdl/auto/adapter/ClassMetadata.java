package com.bdl.auto.adapter;

import com.google.auto.value.AutoValue;
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

  /** Enumeration of the possible types to AudoAdapt: Class and Interface. */
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

  /** The AutoAdaptee's {@link Category}. */
  abstract Category category();

  /** Contains the complete type metadata for the class. */
  abstract TypeMetadata type();

  abstract ImmutableSet<ConstructorMetadata> constructors();

  abstract ImmutableSet<MethodMetadata> abstractMethods();

  abstract ImmutableSet<MethodMetadata> implementedMethods();

  String nestedClassName() {
    return type().nameBuilder()
        .addNestingPrefix()
        .addSimpleName()
        .toString();
  }

  String fullyQualifiedPathName() {
    return type().nameBuilder()
        .addPackagePrefix()
        .addNestingPrefix()
        .addSimpleName()
        .toString();
  }

  String decoratedName(String suffix) {
    return type()
        .nameBuilder()
        .append("AutoAdapter_")
        .addDecoratedNamePrefix()
        .addSimpleName()
        .append("_")
        .append(suffix)
        .toString();
  }

  String fullTypeParams() {
    return type().nameBuilder().addFullParams().toString();
  }

  String unboundedTypeParams() {
    return type().nameBuilder().addSimpleParams().toString();
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
    abstract Builder setCategory(Category category);

    abstract Builder setType(TypeMetadata type);

    abstract ImmutableSet.Builder<ConstructorMetadata> constructorsBuilder();

    abstract ImmutableSet.Builder<MethodMetadata> abstractMethodsBuilder();

    abstract ImmutableSet.Builder<MethodMetadata> implementedMethodsBuilder();

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
