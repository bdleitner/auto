package com.bdl.auto.adapter;

import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

/**
 * Encapsulation of Metadata information for a Type reference.
 *
 * @author Ben Leitner
 */
@AutoValue
abstract class TypeMetadata implements GeneratesImports, Comparable<TypeMetadata> {

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

  @Override
  public int compareTo(TypeMetadata that) {
    return ComparisonChain.start()
        .compare(name(), that.name())
        .compare(outerClassNames(), that.outerClassNames(), Comparators.forLists())
        .compare(packageName(), that.packageName())
        .result();
  }

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
      if (!isTypeParameter()) {
        // TODO: Remove Nesting Prefix and force qualified class names for inner classes?
        allImports.add(nameBuilder().addPackagePrefix().addNestingPrefix().addSimpleName().toString());
      }
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

  String fullDescription() {
    return nameBuilder()
        .addPackagePrefix()
        .addNestingPrefix()
        .addSimpleName()
        .addFullParams()
        .addBounds()
        .toString();
  }

  TypeMetadata convertTypeParams(List<TypeMetadata> newParams) {
    if (!isTypeParameter()) {
      Preconditions.checkArgument(newParams.size() == params().size(),
          "Cannot convert %s to using type params <%s>, the number of params does not match.",
          fullDescription(),
          Joiner.on(", ").join(Iterables.transform(newParams, new Function<TypeMetadata, String>() {
            @Override
            public String apply(TypeMetadata input) {
              return input.name();
            }
          })));
      ImmutableMap.Builder<String, String> paramNameMapBuilder = ImmutableMap.builder();
      int i = 0;
      for (TypeMetadata param : params()) {
        paramNameMapBuilder.put(param.name(), newParams.get(i).name());
        i++;
      }
      return convertTypeParams(paramNameMapBuilder.build());
    } else {
      Preconditions.checkArgument(newParams.size() == 1,
          "Cannot convert %s to type params <%s>, exactly 1 type parameter is required.",
          fullDescription(),
          Joiner.on(", ").join(Iterables.transform(newParams, new Function<TypeMetadata, String>() {
            @Override
            public String apply(TypeMetadata input) {
              return input.name();
            }
          })));
      return convertTypeParams(ImmutableMap.of(name(), newParams.get(0).name()));
    }
  }

  private TypeMetadata convertTypeParams(Map<String, String> paramNameMap) {
    Builder builder = builder()
        .setPackageName(packageName())
        .setIsTypeParameter(isTypeParameter())
        .setName(name());
    builder.outerClassNamesBuilder().addAll(outerClassNames());

    if (!isTypeParameter()) {
      for (TypeMetadata param : params()) {
        builder.addParam(param.convertTypeParams(paramNameMap));
      }
      return builder.build();
    }

    if (paramNameMap.containsKey(name())) {
      builder.setName(paramNameMap.get(name()));
    }
    for (TypeMetadata bound : bounds()) {
      builder.addBound(bound.convertTypeParams(paramNameMap));
    }
    return builder.build();
  }

  @Override
  public String toString() {
    return fullDescription();
  }

  private static String getSimpleName(TypeMirror type) {
    if (type instanceof DeclaredType) {
      return ((DeclaredType) type).asElement().getSimpleName().toString();
    }
    if (type instanceof TypeVariable) {
      return ((TypeVariable) type).asElement().getSimpleName().toString();
    }
    if (type instanceof PrimitiveType || type instanceof NoType || type instanceof NullType) {
      return type.toString();
    }
    throw new IllegalArgumentException(String.format("Cannot determine name for type: %s (%s)",
        type, type.getClass()));
  }

  private static String getQualifiedName(TypeMirror type) {
    if (type instanceof DeclaredType) {
      return ((QualifiedNameable) ((DeclaredType) type).asElement()).getQualifiedName().toString();
    }
    if (type instanceof TypeVariable) {
      return ((QualifiedNameable) ((TypeVariable) type).asElement()).getQualifiedName().toString();
    }
    throw new IllegalArgumentException("Cannot determine name for type: " + type);
  }

  private static TypeMetadata fromType(TypeMirror type, boolean withBounds) {
    Builder builder = builder().setName(getSimpleName(type));

    if (type.getKind() == TypeKind.TYPEVAR) {
      builder.setIsTypeParameter(true);
      TypeVariable typeVar = (TypeVariable) type;
      if (withBounds) {
        TypeMirror upperBound = typeVar.getUpperBound();
        if (upperBound instanceof IntersectionType) {
          for (TypeMirror bound : ((IntersectionType) upperBound).getBounds()) {
            if (getQualifiedName(bound).equals("java.lang.Object")) {
              continue;
            }
            builder.addBound(fromType(bound, false));

          }
        } else {
          if (!getQualifiedName(upperBound).equals("java.lang.Object")) {
            builder.addBound(fromType(upperBound, false));
          }
        }
      }
      return builder.build();
    }

    if (type instanceof DeclaredType) {
      for (TypeMirror param : ((DeclaredType) type).getTypeArguments()) {
        builder.addParam(fromType(param, withBounds));
      }
      Element enclosingElement = ((DeclaredType) type).asElement().getEnclosingElement();
      while (enclosingElement.getKind() != ElementKind.PACKAGE) {
        builder.addOuterClass(enclosingElement.getSimpleName().toString());
        enclosingElement = enclosingElement.getEnclosingElement();
      }

      builder.setPackageName(((QualifiedNameable) enclosingElement).getQualifiedName().toString());
    }
    return builder.build();
  }

  static TypeMetadata fromType(TypeMirror type) {
    return fromType(type, true);
  }

  static TypeMetadata fromElement(Element element) {
    return fromType(element.asType(), true);
  }

  static TypeMetadata simpleTypeParam(String paramName) {
    return builder().setIsTypeParameter(true).setName(paramName).build();
  }

  static Builder builder() {
    return new AutoValue_TypeMetadata.Builder()
        .setPackageName("")
        .setIsTypeParameter(false);
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
            "Type parameters given for type-parameter: %s", metadata.nameBuilder().addSimpleName().addBounds().toString());
        Preconditions.checkState(metadata.outerClassNames().isEmpty(),
            "Nesting classes given type-parameter: %s", metadata.nameBuilder().addSimpleName().addBounds().toString());
        Preconditions.checkState(metadata.packageName().isEmpty(),
            "Nonempty package given for type-parameter: %s", metadata.nameBuilder().addSimpleName().addBounds().toString());
      } else {
        Preconditions.checkState(metadata.bounds().isEmpty(),
            "Bounds given for non-type-parameter: %s", metadata.fullDescription());
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

    TypeNameBuilder append(String s) {
      nameBuilder.append(s);
      return this;
    }

    @Override
    public String toString() {
      return nameBuilder.toString();
    }
  }
}
