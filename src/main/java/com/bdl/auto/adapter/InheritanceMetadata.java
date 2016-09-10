package com.bdl.auto.adapter;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import java.util.Set;

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
  abstract ImmutableList<MethodMetadata> abstractMethods();

  /** Methods that are implemented abstractly in this type. */
  abstract ImmutableList<MethodMetadata> implementedMethods();

  ImmutableList<MethodMetadata> getOrderedNeededMethods() {
    if (orderedNeededMethods == null) {
      Set<MethodMetadata> neededMethods = Sets.newHashSet();
      for (InheritanceMetadata inheritance : inheritances()) {
        neededMethods.addAll(inheritance.getOrderedNeededMethods());
      }
      neededMethods.addAll(abstractMethods());
      neededMethods.removeAll(implementedMethods());
      // TODO: Convert methods for type parameters.
    }
    return orderedNeededMethods;
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract ImmutableList.Builder<TypeMetadata> inheritanceParamsBuilder();
    abstract Builder setType(TypeMetadata type);
    abstract ImmutableList.Builder<InheritanceMetadata> inheritancesBuilder();
    abstract ImmutableList.Builder<MethodMetadata> abstractMethodsBuilder();
    abstract ImmutableList.Builder<MethodMetadata> implementedMethodsBuilder();

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

    Builder addAbstractMethod(MethodMetadata method) {
      abstractMethodsBuilder().add(method);
      return this;
    }

    Builder addImplementedMethod(MethodMetadata method) {
      implementedMethodsBuilder().add(method);
      return this;
    }

    abstract InheritanceMetadata build();
  }
}
