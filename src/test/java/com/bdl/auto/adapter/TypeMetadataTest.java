package com.bdl.auto.adapter;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.testing.compile.CompilationRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Tests for the TypeMetadata class.
 *
 * @author Ben Leitner
 */
@RunWith(JUnit4.class)
public class TypeMetadataTest {

  @Rule public final CompilationRule compilation = new CompilationRule();

  private Elements elements;

  @Before
  public void before() {
    elements = compilation.getElements();
  }

  @Test
  public void testSimpleInterface() {
    TypeElement element = elements.getTypeElement("com.bdl.auto.adapter.Simple");
    TypeMetadata type = TypeMetadata.fromElement(element);
    assertThat(type).isEqualTo(
        TypeMetadata.builder()
            .setPackageName("com.bdl.auto.adapter")
            .setName("Simple")
            .build());

    assertThat(type.nameBuilder().addSimpleName().toString())
        .isEqualTo("Simple");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.Simple");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .addFullParams()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.Simple");
  }

  @Test
  public void testParameterizedInterface() {
    TypeElement element = elements.getTypeElement("com.bdl.auto.adapter.Parameterized");
    TypeMetadata type = TypeMetadata.fromElement(element);
    assertThat(type).isEqualTo(
        TypeMetadata.builder()
            .setPackageName("com.bdl.auto.adapter")
            .setName("Parameterized")
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("T")
                .build())
            .build());

    assertThat(type.nameBuilder().addSimpleName().toString())
        .isEqualTo("Parameterized");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.Parameterized");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .addSimpleParams()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.Parameterized<T>");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .addFullParams()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.Parameterized<T>");
  }

  @Test
  public void testParameterizedWithBound() {
    TypeElement element = elements.getTypeElement("com.bdl.auto.adapter.Field");
    TypeMetadata type = TypeMetadata.fromElement(element);
    assertThat(type).isEqualTo(
        TypeMetadata.builder()
            .setPackageName("com.bdl.auto.adapter")
            .setName("Field")
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("F")
                .addBound(TypeMetadata.builder()
                    .setPackageName("com.bdl.auto.adapter")
                    .setName("Field")
                    .addParam(TypeMetadata.builder()
                        .setIsTypeParameter(true)
                        .setName("F")
                        .build())
                    .build())
                .build())
            .build());

    assertThat(type.nameBuilder().addSimpleName().toString())
        .isEqualTo("Field");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.Field");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .addSimpleParams()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.Field<F>");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .addFullParams()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.Field<F extends com.bdl.auto.adapter.Field<F>>");
  }

  @Test
  public void testMultipleParamsMultipleBounds() {
    TypeElement element = elements.getTypeElement("com.bdl.auto.adapter.ParameterizedMultibound");
    TypeMetadata type = TypeMetadata.fromElement(element);
    assertThat(type).isEqualTo(
        TypeMetadata.builder()
            .setPackageName("com.bdl.auto.adapter")
            .setName("ParameterizedMultibound")
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("S")
                .build())
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("T")
                .addBound(TypeMetadata.builder()
                    .setPackageName("com.bdl.auto.adapter")
                    .setName("Simple")
                    .build())
                .addBound(TypeMetadata.builder()
                    .setPackageName("com.bdl.auto.adapter")
                    .setName("Parameterized")
                    .addParam(TypeMetadata.builder()
                        .setIsTypeParameter(true)
                        .setName("S")
                        .build())
                    .build())
                .build())
            .build());

    assertThat(type.nameBuilder().addSimpleName().toString())
        .isEqualTo("ParameterizedMultibound");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.ParameterizedMultibound");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .addSimpleParams()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.ParameterizedMultibound<S, T>");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .addFullParams()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.ParameterizedMultibound"
            + "<S, T extends com.bdl.auto.adapter.Simple & com.bdl.auto.adapter.Parameterized<S>>");
  }

  @Test
  public void testNestedClasses() {
    TypeElement element = elements.getTypeElement("com.bdl.auto.adapter.TopLevel.Outer.Inner");
    TypeMetadata type = TypeMetadata.fromElement(element);
    assertThat(type).isEqualTo(
        TypeMetadata.builder()
            .setPackageName("com.bdl.auto.adapter")
            .setName("Inner")
            .addOuterClass("Outer")
            .addOuterClass("TopLevel")
            .build());

    assertThat(type.nameBuilder().addSimpleName().toString())
        .isEqualTo("Inner");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.Inner");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addNestingPrefix()
        .addSimpleName()
        .addSimpleParams()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.TopLevel.Outer.Inner");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addNestingPrefix()
        .addSimpleName()
        .addFullParams()
        .addBounds()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.TopLevel.Outer.Inner");
  }

  @Test
  public void testGenerateImports() {
    TypeElement element = elements.getTypeElement("com.bdl.auto.adapter.ParameterizedMultibound");
    TypeMetadata type = TypeMetadata.fromElement(element);
    assertThat(type.getImports())
        .containsExactly(
            "com.bdl.auto.adapter.Parameterized",
            "com.bdl.auto.adapter.ParameterizedMultibound",
            "com.bdl.auto.adapter.Simple")
        .inOrder();

  }

  @Test
  public void testComplexParameterization() {
    TypeElement element = elements.getTypeElement("com.bdl.auto.adapter.ComplexParameterized");
    TypeMetadata type = TypeMetadata.fromElement(element);
    assertThat(type).isEqualTo(
        TypeMetadata.builder()
            .setPackageName("com.bdl.auto.adapter")
            .setName("ComplexParameterized")
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("X")
                .build())
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("Y")
                .addBound(TypeMetadata.builder()
                    .setPackageName("java.lang")
                    .setName("Comparable")
                    .addParam(TypeMetadata.builder()
                        .setIsTypeParameter(true)
                        .setName("Y")
                        .build())
                    .build())
                .build())
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("Z")
                .addBound(TypeMetadata.builder()
                    .setPackageName("java.util")
                    .setName("List")
                    .addParam(TypeMetadata.builder()
                        .setIsTypeParameter(true)
                        .setName("Y")
                        .build())
                    .build())
                .build())
            .build());

    assertThat(type.nameBuilder().addSimpleName().toString())
        .isEqualTo("ComplexParameterized");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.ComplexParameterized");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .addSimpleParams()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.ComplexParameterized<X, Y, Z>");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .addFullParams()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.ComplexParameterized<"
            + "X, Y extends java.lang.Comparable<Y>, Z extends java.util.List<Y>>");
  }

  @Test
  public void testTypeConversion() {
    TypeElement element = elements.getTypeElement("com.bdl.auto.adapter.ComplexParameterized");
    TypeMetadata type = TypeMetadata.fromElement(element);
    type = type.convertTypeParams(ImmutableList.of(
        TypeMetadata.simpleTypeParam("A"),
        TypeMetadata.simpleTypeParam("B"),
        TypeMetadata.simpleTypeParam("C")));
    assertThat(type).isEqualTo(
        TypeMetadata.builder()
            .setPackageName("com.bdl.auto.adapter")
            .setName("ComplexParameterized")
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("A")
                .build())
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("B")
                .addBound(TypeMetadata.builder()
                    .setPackageName("java.lang")
                    .setName("Comparable")
                    .addParam(TypeMetadata.builder()
                        .setIsTypeParameter(true)
                        .setName("B")
                        .build())
                    .build())
                .build())
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("C")
                .addBound(TypeMetadata.builder()
                    .setPackageName("java.util")
                    .setName("List")
                    .addParam(TypeMetadata.builder()
                        .setIsTypeParameter(true)
                        .setName("B")
                        .build())
                    .build())
                .build())
            .build());

    assertThat(type.nameBuilder().addSimpleName().toString())
        .isEqualTo("ComplexParameterized");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.ComplexParameterized");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .addSimpleParams()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.ComplexParameterized<A, B, C>");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .addFullParams()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.ComplexParameterized<"
            + "A, B extends java.lang.Comparable<B>, C extends java.util.List<B>>");
  }
}