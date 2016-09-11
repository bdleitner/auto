package com.bdl.auto.adapter;

import static com.bdl.auto.adapter.TypeMetadata.simpleTypeParam;
import static com.google.common.truth.Truth.assertThat;

import com.google.testing.compile.CompilationRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Tests for the ClassMetadata class.
 *
 * @author Ben Leitner
 */
public class ClassMetadataTest {

  @Rule public final CompilationRule compilation = new CompilationRule();

  private ClassMetadata metadata;

  @Before
  public void before() {
    Elements elements = compilation.getElements();
    TypeElement element = elements.getTypeElement("com.bdl.auto.adapter.AbstractClass");
    metadata = ClassMetadata.fromElement(element);
  }

  @Test
  public void testRequiredMethods() {
    TypeMetadata typeEExtendsListOfD = TypeMetadata.builder()
        .setName("E")
        .addBound(TypeMetadata.builder()
            .setPackageName("java.util")
            .setName("List")
            .addParam(simpleTypeParam("D"))
            .build())
        .build();
    assertThat(metadata.getOrderedRequiredMethods()).containsExactly(
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .setType(TypeMetadata.STRING)
            .setName("thingToString")
            .addParameter(ParameterMetadata.of(TestingTypes.THING, "input"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .setType(TypeMetadata.INT)
            .setName("add")
            .addParameter(ParameterMetadata.of(TypeMetadata.INT, "first"))
            .addParameter(ParameterMetadata.of(TypeMetadata.INT, "second"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .setType(simpleTypeParam("A"))
            .setName("extendedFrozzle")
            .addParameter(ParameterMetadata.of(simpleTypeParam("A"), "input"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .setType(simpleTypeParam("A"))
            .setName("superExtendedFrozzle")
            .addParameter(ParameterMetadata.of(simpleTypeParam("A"), "input"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .addTypeParameter(simpleTypeParam("D"))
            .addTypeParameter(typeEExtendsListOfD)
            .setType(TypeMetadata.builder()
                .setPackageName("com.google.common.collect")
                .setName("ImmutableList")
                .addParam(simpleTypeParam("D"))
                .build())
            .setName("filter")
            .addParameter(ParameterMetadata.of(typeEExtendsListOfD, "source"))
            .addParameter(ParameterMetadata.of(
                TypeMetadata.builder()
                    .setPackageName("com.google.common.base")
                    .setName("Predicate")
                    .addParam(simpleTypeParam("D"))
                    .build(),
                "predicate"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .addTypeParameter(simpleTypeParam("T"))
            .setType(simpleTypeParam("T"))
            .setName("extend")
            .addParameter(ParameterMetadata.of(
                TypeMetadata.builder()
                    .setIsTypeParameter(true)
                    .setName("C")
                    .addBound(TypeMetadata.builder()
                        .setPackageName("java.util")
                        .setName("List")
                        .addParam(simpleTypeParam("B"))
                        .build())
                    .build(),
                "list"))
            .addParameter(ParameterMetadata.of(simpleTypeParam("T"), "template"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .setType(simpleTypeParam("B"))
            .setName("blargh")
            .addParameter(ParameterMetadata.of(simpleTypeParam("B"), "input"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .setType(TypeMetadata.VOID)
            .setName("doNothing")
            .addParameter(ParameterMetadata.of(TypeMetadata.STRING, "input"))
            .build(),
        MethodMetadata.builder()
            .setVisibility(Visibility.PROTECTED)
            .setIsAbstract(true)
            .setType(TypeMetadata.VOID)
            .setName("voidFromSuper")
            .addParameter(ParameterMetadata.of(TypeMetadata.INT, "foo"))
            .build());
  }

  @Test
  public void testRequiredConstructors() {
    assertThat(metadata.getOrderedRequiredConstructors()).containsExactly(
        ConstructorMetadata.builder()
            .visibility(Visibility.PACKAGE_LOCAL)
            .addParameter(ParameterMetadata.of(TypeMetadata.INT, "foo"))
            .build(),
        ConstructorMetadata.builder()
            .visibility(Visibility.PUBLIC)
            .addParameter(ParameterMetadata.of(TypeMetadata.BOOLEAN, "foo"))
            .build());
  }
}
