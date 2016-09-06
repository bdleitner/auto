package com.bdl.auto.adapter;

import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

/**
 * Encapsulation of Metadata information for a Type reference.
 *
 * @author Ben Leitner
 */
@AutoValue
abstract class TypeMetadata implements GeneratesImports {

  private ImmutableList<String> imports;

  /** The package in which the type lives. */
  abstract String packageName();

  /** If {@code true}, the type is a Generic type parameter. */
  abstract boolean isTypeParameter();

  /** The names of any outer classes enclosing this type, from innermost to outermost. */
  abstract ImmutableList<String> outerClassNames();

  /** The name of the type. */
  abstract String name();

  /** Type parameters for a generic type. */
  abstract ImmutableList<TypeMetadata> params();

  /** Bounds for a type parameter type. */
  abstract ImmutableList<TypeMetadata> bounds();

  String fullyQualifiedPathName() {
    return nameBuilder()
        .addPackagePrefix()
        .addNestingPrefix()
        .addSimpleName()
        .toString();
  }

  /** Get a builder to construct a type name. */
  TypeNameBuilder nameBuilder() {
    return new TypeNameBuilder();
  }

  @Override
  public ImmutableList<String> getImports() {
    if (imports == null) {
      ImmutableSet.Builder<String> allImports = ImmutableSet.builder();
      for (TypeMetadata param : params()) {
        allImports.addAll(param.getImports());
      }
      for (TypeMetadata bound : bounds()) {
        allImports.addAll(bound.getImports());
      }

      List<String> importList = Lists.newArrayList(allImports.build());
      Collections.sort(importList);
      imports = ImmutableList.copyOf(importList);
    }
    return imports;
  }

  @Override
  public String toString() {
    return fullyQualifiedPathName();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setPackageName(String packageName);

    abstract Builder setIsTypeParameter(boolean isTypeParameter);

    abstract ImmutableList.Builder<String> outerClassNamesBuilder();

    abstract Builder setName(String name);

    abstract ImmutableList.Builder<TypeMetadata> paramsBuilder();

    abstract ImmutableList.Builder<TypeMetadata> boundsBuilder();

    Builder addOuterClass(String className) {
      setIsTypeParameter(false);
      outerClassNamesBuilder().add(className);
      return this;
    }

    Builder addParam(TypeMetadata metadata) {
      paramsBuilder().add(metadata);
      return this;
    }

    Builder addBound(TypeMetadata metadata) {
      setIsTypeParameter(true);
      boundsBuilder().add(metadata);
      return this;
    }

    abstract TypeMetadata autoBuild();

    TypeMetadata build() {
      TypeMetadata metadata = autoBuild();
      if (metadata.isTypeParameter()) {
        Preconditions.checkState(metadata.params().isEmpty(),
            "Type parameters given for type-parameter: %s", metadata);
        Preconditions.checkState(metadata.outerClassNames().isEmpty(),
            "Nesting classes given type-parameter: %s", metadata);
        Preconditions.checkState(metadata.packageName().isEmpty(),
            "Nonempty package given for type-parameter: %s", metadata);
      } else {
        Preconditions.checkState(metadata.bounds().isEmpty(),
            "Bounds given for non-type-parameter: %s", metadata);
      }
      return metadata;
    }
  }

  // TODO: Incorporate shorter names if we have imports available.
  class TypeNameBuilder {
    private final StringBuilder nameBuilder;

    TypeNameBuilder() {
      nameBuilder = new StringBuilder();
    }

    private TypeNameBuilder addNestingPrefix(String delimiter) {
      if (!outerClassNames().isEmpty()) {
        nameBuilder.append(Joiner.on(delimiter).join(Lists.reverse(outerClassNames()))).append(delimiter);
      }
      return this;
    }

    TypeNameBuilder addPackagePrefix() {
      if (!packageName().isEmpty()) {
        nameBuilder.append(packageName()).append(".");
      }
      return this;
    }

    TypeNameBuilder addNestingPrefix() {
      return addNestingPrefix(".");
    }

    TypeNameBuilder addDecoratedNamePrefix() {
      return addNestingPrefix("_");
    }

    TypeNameBuilder addSimpleName() {
      nameBuilder.append(name());
      return this;
    }

    private TypeNameBuilder addParams(Function<TypeMetadata, String> paramsToStrings) {
      if (!params().isEmpty()) {
        nameBuilder
            .append("<")
            .append(Joiner.on(", ").join(Iterables.transform(params(), paramsToStrings)))
            .append(">");
      }
      return this;
    }

    TypeNameBuilder addSimpleParams() {
      return addParams(new Function<TypeMetadata, String>() {
        @Override
        public String apply(TypeMetadata param) {
          return param.nameBuilder()
              .addPackagePrefix()
              .addNestingPrefix()
              .addSimpleName()
              .addSimpleParams()
              .toString();
        }
      });
    }

    TypeNameBuilder addFullParams() {
      return addParams(new Function<TypeMetadata, String>() {
        @Override
        public String apply(TypeMetadata param) {
          return param.nameBuilder()
              .addPackagePrefix()
              .addNestingPrefix()
              .addSimpleName()
              .addSimpleParams() // Note, there cannot be both simple params and bounds.
              .addBounds() // as one only applies to type params and one to non-type-params.
              .toString();
        }
      });
    }

    TypeNameBuilder addBounds() {
      if (!bounds().isEmpty()) {
        nameBuilder
            .append(" extends ")
            .append(Joiner.on(" & ").join(
                Iterables.transform(
                    bounds(),
                    new Function<TypeMetadata, String>() {
                      @Override
                      public String apply(TypeMetadata bound) {
                        return bound.nameBuilder()
                            .addPackagePrefix()
                            .addNestingPrefix()
                            .addSimpleName()
                            .addSimpleParams() // Note, there cannot be both simple params and bounds.
                            .addBounds() // as one only applies to type params and one to non-type-params.
                            .toString();
                      }
                    }
                )));
      }
      return this;
    }

    @Override
    public String toString() {
      return nameBuilder.toString();
    }
  }
}
