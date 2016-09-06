package com.bdl.auto.adapter;

import static com.google.common.truth.Truth.assertThat;

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
    TypeElement element = elements.getTypeElement("com.bdl.auto.adapter.SimpleInterface");
    TypeMetadata type = TypeMetadata.fromElement(element);
    assertThat(type).isEqualTo(
        TypeMetadata.builder()
            .setPackageName("com.bdl.auto.adapter")
            .setName("SimpleInterface")
            .build());

    assertThat(type.nameBuilder().addSimpleName().toString())
        .isEqualTo("SimpleInterface");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.SimpleInterface");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .addFullParams()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.SimpleInterface");
  }

  @Test
  public void testParameterizedInterface() {
    TypeElement element = elements.getTypeElement("com.bdl.auto.adapter.ParameterizedInterface");
    TypeMetadata type = TypeMetadata.fromElement(element);
    assertThat(type).isEqualTo(
        TypeMetadata.builder()
            .setPackageName("com.bdl.auto.adapter")
            .setName("ParameterizedInterface")
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("T")
                .build())
            .build());

    assertThat(type.nameBuilder().addSimpleName().toString())
        .isEqualTo("ParameterizedInterface");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.ParameterizedInterface");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .addSimpleParams()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.ParameterizedInterface<T>");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .addFullParams()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.ParameterizedInterface<T>");
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
    TypeElement element = elements.getTypeElement("com.bdl.auto.adapter.ParameterizedMultiboundInterface");
    TypeMetadata type = TypeMetadata.fromElement(element);
    assertThat(type).isEqualTo(
        TypeMetadata.builder()
            .setPackageName("com.bdl.auto.adapter")
            .setName("ParameterizedMultiboundInterface")
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("S")
                .build())
            .addParam(TypeMetadata.builder()
                .setIsTypeParameter(true)
                .setName("T")
                .addBound(TypeMetadata.builder()
                    .setPackageName("com.bdl.auto.adapter")
                    .setName("SimpleInterface")
                    .build())
                .addBound(TypeMetadata.builder()
                    .setPackageName("com.bdl.auto.adapter")
                    .setName("ParameterizedInterface")
                    .addParam(TypeMetadata.builder()
                        .setIsTypeParameter(true)
                        .setName("S")
                        .build())
                    .build())
                .build())
            .build());

    assertThat(type.nameBuilder().addSimpleName().toString())
        .isEqualTo("ParameterizedMultiboundInterface");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.ParameterizedMultiboundInterface");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .addSimpleParams()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.ParameterizedMultiboundInterface<S, T>");

    assertThat(type.nameBuilder()
        .addPackagePrefix()
        .addSimpleName()
        .addFullParams()
        .toString())
        .isEqualTo("com.bdl.auto.adapter.ParameterizedMultiboundInterface"
            + "<S, T extends com.bdl.auto.adapter.SimpleInterface & com.bdl.auto.adapter.ParameterizedInterface<S>>");
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
    TypeElement element = elements.getTypeElement("com.bdl.auto.adapter.ParameterizedMultiboundInterface");
    TypeMetadata type = TypeMetadata.fromElement(element);
    assertThat(type.getImports())
        .containsExactly(
            "com.bdl.auto.adapter.ParameterizedInterface",
            "com.bdl.auto.adapter.ParameterizedMultiboundInterface",
            "com.bdl.auto.adapter.SimpleInterface")
        .inOrder();

  }
}