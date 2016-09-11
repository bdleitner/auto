package com.bdl.auto.adapter;

import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * Metadata class for a relevant parts of a class to write.
 *
 * @author Ben Leitner
 */
@AutoValue
abstract class ClassMetadata implements GeneratesImports, GeneratesMethods {

  private static final Predicate<MethodMetadata> ABSTRACT_PREDICATE = new Predicate<MethodMetadata>() {
    @Override
    public boolean apply(MethodMetadata input) {
      return input.isAbstract();
    }
  };

  private static final Predicate<MethodMetadata> CONCRETE_PREDICATE = Predicates.not(ABSTRACT_PREDICATE);
  private static final Function<MethodMetadata, MethodMetadata> TO_ABSTRACT
      = new Function<MethodMetadata, MethodMetadata>() {
    @Override
    public MethodMetadata apply(MethodMetadata input) {
      return input.toBuilder().setIsAbstract(true).build();
    }
  };

  /** Enumeration of the possible types to AutoAdapt: Class and Interface. */
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
  private ImmutableList<MethodMetadata> allMethods;
  private ImmutableList<MethodMetadata> orderedRequiredMethods;

  /** The AutoAdaptee's {@link Category}. */
  abstract Category category();

  /** Contains the complete type metadata for the class. */
  abstract TypeMetadata type();

  /** The inheritance metadatas for the types that this one inherits from. */
  abstract ImmutableList<InheritanceMetadata> inheritances();

  abstract ImmutableSet<ConstructorMetadata> constructors();

  /** Methods that are declared in this type. */
  abstract ImmutableList<MethodMetadata> methods();

  @Override
  public ImmutableList<String> getImports() {
    Set<String> imports = Sets.newHashSet();
    imports.addAll(type().getImports());
    for (InheritanceMetadata inheritance : inheritances()) {
      imports.addAll(inheritance.getImports());
    }
    for (ConstructorMetadata constructor : constructors()) {
      imports.addAll(constructor.getImports());
    }
    for (MethodMetadata method : methods()) {
      imports.addAll(method.getImports());
    }
    List<String> allImports = Lists.newArrayList(imports);
    Collections.sort(allImports);
    return ImmutableList.copyOf(allImports);
  }

  ImmutableList<ConstructorMetadata> getOrderedRequiredConstructors() {
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

  @Override
  public ImmutableList<MethodMetadata> getAllMethods() {
    if (allMethods == null) {
      Set<MethodMetadata> methods = Sets.newHashSet();
      for (InheritanceMetadata inheritance : inheritances()) {
        methods.addAll(inheritance.getAllMethods());
      }

      methods.addAll(methods());
      allMethods = ImmutableList.copyOf(methods);
    }
    return allMethods;
  }

  public ImmutableList<MethodMetadata> getOrderedRequiredMethods() {
    if (orderedRequiredMethods == null) {
      ImmutableList<MethodMetadata> allMethods = getAllMethods();

      Set<MethodMetadata> abstractMethods = Sets.newHashSet(Iterables.filter(allMethods, ABSTRACT_PREDICATE));

      abstractMethods.removeAll(
          ImmutableSet.copyOf(Iterables.transform(Iterables.filter(allMethods, CONCRETE_PREDICATE), TO_ABSTRACT)));

      List<MethodMetadata> ordered = Lists.newArrayList(abstractMethods);
      Collections.sort(ordered);
      orderedRequiredMethods = ImmutableList.copyOf(ordered);
    }
    return orderedRequiredMethods;
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

  @Override
  public String toString() {
    return fullyQualifiedPathName();
  }

  static ClassMetadata fromElement(Element element) {
    Builder metadata = builder()
        .setCategory(Category.forKind(element.getKind()))
        .setType(TypeMetadata.fromElement(element));

    TypeElement typeElement = (TypeElement) element;
    TypeMirror superClass = typeElement.getSuperclass();
    if (superClass instanceof DeclaredType) {
      metadata.addInheritance(InheritanceMetadata.fromType((DeclaredType) superClass));
    }

    for (TypeMirror inherited : typeElement.getInterfaces()) {
      metadata.addInheritance(InheritanceMetadata.fromType((DeclaredType) inherited));
    }

    for (Element enclosed : element.getEnclosedElements()) {
      if (enclosed.getKind() == ElementKind.METHOD) {
        metadata.addMethod(MethodMetadata.fromMethod((ExecutableElement) enclosed));
      }
      if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
        metadata.addConstructor(ConstructorMetadata.fromConstructor(enclosed));
      }
    }
    return metadata.build();
  }

  static Builder builder() {
    return new AutoValue_ClassMetadata.Builder();
  }

  @AutoValue.Builder
  static abstract class Builder {
    abstract Builder setCategory(Category category);

    abstract Builder setType(TypeMetadata type);

    abstract ImmutableList.Builder<InheritanceMetadata> inheritancesBuilder();

    abstract ImmutableSet.Builder<ConstructorMetadata> constructorsBuilder();

    abstract ImmutableList.Builder<MethodMetadata> methodsBuilder();

    Builder addInheritance(InheritanceMetadata inheritance) {
      inheritancesBuilder().add(inheritance);
      return this;
    }

    Builder addConstructor(ConstructorMetadata constructor) {
      constructorsBuilder().add(constructor);
      return this;
    }

    Builder addMethod(MethodMetadata method) {
      methodsBuilder().add(method);
      return this;
    }

    abstract ClassMetadata build();
  }
}
