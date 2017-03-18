package com.bdl.auto.impl.processor;

import com.bdl.annotation.processing.model.AnnotationMetadata;
import com.bdl.auto.impl.AutoImpl;
import com.bdl.auto.impl.ImplOption;
import com.google.testing.compile.CompilationRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public class AnnotationUtilTest {

  @Rule
  public final CompilationRule compilation = new CompilationRule();

  private Elements elements;

  @Before
  public void before() {
    elements = compilation.getElements();
  }

  @Test
  public void testAutoImplAnnotation() {
    TypeElement typeElement = elements.getTypeElement("com.bdl.auto.impl.processor.HasOverrides");
    AnnotationMetadata metadata = AnnotationMetadata.fromType(typeElement.getAnnotationMirrors().get(0));
    AutoImpl actual = AnnotationUtil.autoImpl(metadata);
    assertThat(actual).isEqualTo(
        AnnotationUtil.autoImpl(
            ImplOption.THROW_EXCEPTION,
            ImplOption.RETURN_DEFAULT_VALUE,
            ImplOption.RETURN_DEFAULT_VALUE,
            ImplOption.USE_PARENT,
            ImplOption.USE_PARENT,
            ImplOption.USE_PARENT));

  }
}
