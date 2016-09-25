package com.bdl.auto.impl.processor;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.google.testing.compile.CompilationRule;

import com.bdl.annotation.processing.model.ClassMetadata;
import com.bdl.annotation.processing.model.ConstructorMetadata;
import com.bdl.annotation.processing.model.MethodMetadata;
import com.bdl.annotation.processing.model.ParameterMetadata;
import com.bdl.annotation.processing.model.TypeMetadata;
import com.bdl.annotation.processing.model.Visibility;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Tests for the {@linkplain AutoImplWriter} class.
 *
 * @author Ben Leitner
 */
@RunWith(JUnit4.class)
public class AutoImplWriterTest {

  @Rule public final CompilationRule compilation = new CompilationRule();

  private Elements elements;

  @Before
  public void before() {
    elements = compilation.getElements();
  }

  @Test
  public void testSimpleClass() throws Exception {
    ClassMetadata type = ClassMetadata.builder()
        .setCategory(ClassMetadata.Category.CLASS)
        .setType(TypeMetadata.builder()
            .setPackageName("com.bdl.auto.impl.processor")
            .setName("Simple")
            .build())
        .addMethod(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setIsAbstract(true)
                .setType(TypeMetadata.INT)
                .setName("add")
                .addParameter(ParameterMetadata.of(TypeMetadata.INT, "first"))
                .addParameter(ParameterMetadata.of(TypeMetadata.INT, "second"))
                .build())
        .addMethod(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setIsAbstract(true)
                .setType(TypeMetadata.STRING)
                .setName("repeat")
                .addParameter(ParameterMetadata.of(TypeMetadata.STRING, "template"))
                .addParameter(ParameterMetadata.of(TypeMetadata.INT, "times"))
                .build())
        .addMethod(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setIsAbstract(true)
                .setType(TestingTypes.THING)
                .setName("getThing")
                .build())
        .build();

    assertOutput(type);
  }

  @Test
  public void testSimpleInterface() throws Exception {
    ClassMetadata type = ClassMetadata.builder()
        .setCategory(ClassMetadata.Category.INTERFACE)
        .setType(TypeMetadata.builder()
            .setPackageName("com.bdl.auto.impl.processor")
            .setName("SimpleInterface")
            .build())
        .addMethod(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setIsAbstract(true)
                .setType(TypeMetadata.INT)
                .setName("add")
                .addParameter(ParameterMetadata.of(TypeMetadata.INT, "first"))
                .addParameter(ParameterMetadata.of(TypeMetadata.INT, "second"))
                .build())
        .build();

    assertOutput(type);
  }

  @Test
  public void testParameterized() throws Exception {
    ClassMetadata type = ClassMetadata.builder()
        .setCategory(ClassMetadata.Category.INTERFACE)
        .setType(TypeMetadata.builder()
            .setPackageName("com.bdl.auto.impl.processor")
            .setName("Parameterized")
            .addParam(TestingTypes.PARAM_T_EXTENDS_FOO)
            .build())
        .addMethod(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setIsAbstract(true)
                .setType(TestingTypes.PARAM_T)
                .setName("frozzle")
                .addParameter(ParameterMetadata.of(TestingTypes.PARAM_T, "input"))
                .build())
        .build();

    assertOutput(type);
  }

  @Test
  public void testHasAnImplementedMethod() throws Exception {
    MethodMetadata addMethod = MethodMetadata.builder()
        .setVisibility(Visibility.PUBLIC)
        .setType(TypeMetadata.INT)
        .setName("add")
        .addParameter(ParameterMetadata.of(TypeMetadata.INT, "first"))
        .addParameter(ParameterMetadata.of(TypeMetadata.INT, "second"))
        .build();
    MethodMetadata subtractMethod = MethodMetadata.builder()
        .setVisibility(Visibility.PUBLIC)
        .setIsAbstract(true)
        .setType(TypeMetadata.INT)
        .setName("subtract")
        .addParameter(ParameterMetadata.of(TypeMetadata.INT, "first"))
        .addParameter(ParameterMetadata.of(TypeMetadata.INT, "second"))
        .build();
    ClassMetadata type = ClassMetadata.builder()
        .setCategory(ClassMetadata.Category.CLASS)
        .setType(TypeMetadata.builder()
            .setPackageName("com.bdl.auto.impl.processor")
            .setName("Partial")
            .build())
        .addMethod(addMethod)
        .addMethod(subtractMethod)
        .build();

    assertOutput(type);
  }

  @Test
  public void testHasConstructors() throws Exception {
    ClassMetadata type = ClassMetadata.builder()
        .setCategory(ClassMetadata.Category.CLASS)
        .setType(TypeMetadata.builder()
            .setPackageName("com.bdl.auto.impl.processor")
            .setName("Constructable")
            .build())
        .addConstructor(ConstructorMetadata.builder()
            .visibility(Visibility.PUBLIC)
            .addParameter(ParameterMetadata.of(TypeMetadata.INT, "arg1"))
            .addParameter(ParameterMetadata.of(TypeMetadata.STRING, "arg2"))
            .build())
        .addConstructor(ConstructorMetadata.builder()
            .visibility(Visibility.PACKAGE_LOCAL)
            .addParameter(ParameterMetadata.of(TypeMetadata.STRING, "arg1"))
            .build())
        .addMethod(MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .setType(TypeMetadata.INT)
            .setName("add")
            .addParameter(ParameterMetadata.of(TypeMetadata.INT, "first"))
            .addParameter(ParameterMetadata.of(TypeMetadata.INT, "second"))
            .build())
        .addMethod(MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setIsAbstract(true)
            .setType(TypeMetadata.INT)
            .setName("subtract")
            .addParameter(ParameterMetadata.of(TypeMetadata.INT, "first"))
            .addParameter(ParameterMetadata.of(TypeMetadata.INT, "second"))
            .build())
        .build();

    assertOutput(type);
  }

  @Test
  public void testFromLiveData() throws Exception {
    TypeElement element = elements.getTypeElement("com.bdl.auto.impl.processor.AbstractClass");
    ClassMetadata metadata = ClassMetadata.fromElement(element);

    assertOutput(metadata);
  }

  @Test
  public void testOverrides() throws Exception {
    TypeElement element = elements.getTypeElement("com.bdl.auto.impl.processor.HasOverrides");
    ClassMetadata metadata = ClassMetadata.fromElement(element);

    assertOutput(metadata);
  }

  private void assertOutput(ClassMetadata type) throws Exception {
    final Map<String, Writer> writerMap = Maps.newHashMap();

    AutoImplWriter writer = new AutoImplWriter(
        new Function<String, Writer>() {
          @Override
          public Writer apply(String input) {
            StringWriter writer = new StringWriter();
            writerMap.put(input + ".txt", writer);
            return writer;
          }
        });

    String key = String.format("%s.%s.txt", type.type().packageName(),
        type.type().nameBuilder()
        .append("Auto_")
        .addNestingPrefix("_")
        .addSimpleName()
        .append("_")
        .append("Impl")
        .toString());
    writer.write(type);

    URL resource = getClass().getClassLoader().getResource(key);
    String file = Resources.toString(resource, Charsets.UTF_8);

    assertThat(writerMap.get(key).toString()).isEqualTo(file);
  }
}
