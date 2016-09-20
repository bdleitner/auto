package com.bdl.auto.impl.processor;

import static com.google.common.truth.Truth.assertThat;

import com.google.testing.compile.CompilationRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/** Tests for the {@link AnnotationMetadata} class. */
@RunWith(JUnit4.class)
public class AnnotationMetadataTest {

  @Rule public final CompilationRule compilation = new CompilationRule();

  private Elements elements;

  @Before
  public void before() {
    elements = compilation.getElements();
  }

  @Test
  public void testFromElement() {
    TypeMetadata implOption = TestingTypes.IMPL_OPTION;
    TypeElement typeElement = elements.getTypeElement("com.bdl.auto.impl.processor.HasOverrides");
    AnnotationMirror annotationMirror = typeElement.getAnnotationMirrors().get(0);
    AnnotationMetadata actual = AnnotationMetadata.fromType(annotationMirror);
    AnnotationMetadata expected = AnnotationMetadata.builder()
        .setType(TypeMetadata.builder()
            .setPackageName("com.bdl.auto.impl")
            .setName("AutoImpl")
            .build())
        .putValue(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setIsAbstract(true)
                .setType(implOption)
                .setName("value")
                .build(),
            ValueMetadata.create(implOption, "THROW_EXCEPTION"))
        .putValue(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setIsAbstract(true)
                .setType(implOption)
                .setName("booleanImpl")
                .build(),
            ValueMetadata.create(implOption, "RETURN_DEFAULT_VALUE"))
        .putValue(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setIsAbstract(true)
                .setType(implOption)
                .setName("numericImpl")
                .build(),
            ValueMetadata.create(implOption, "RETURN_DEFAULT_VALUE"))
        .putValue(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setIsAbstract(true)
                .setType(implOption)
                .setName("stringImpl")
                .build(),
            ValueMetadata.create(implOption, "USE_PARENT"))
        .build();
    assertThat(actual).isEqualTo(expected);
  }
}
