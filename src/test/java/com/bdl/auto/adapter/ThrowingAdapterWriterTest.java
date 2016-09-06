package com.bdl.auto.adapter;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Map;

/**
 * Tests for the {@linkplain DefaultValuesAdapterWriter} class.
 *
 * @author Ben Leitner
 */
@RunWith(JUnit4.class)
public class ThrowingAdapterWriterTest {

  @Test
  public void testSimpleClass() throws Exception {
    ClassMetadata type = ClassMetadata.builder()
        .packageName("com.bdl.auto.adapter")
        .category(ClassMetadata.Category.CLASS)
        .name("Simple")
        .addAbstractMethod(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setType("int")
                .setName("add")
                .addParameter(ParameterMetadata.of("int", "first"))
                .addParameter(ParameterMetadata.of("int", "second"))
                .build())
        .addAbstractMethod(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setType("String")
                .setName("repeat")
                .addParameter(ParameterMetadata.of("String", "template"))
                .addParameter(ParameterMetadata.of("int", "times"))
                .build())
        .addAbstractMethod(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setType("com.bdl.auto.adapter.Thing")
                .setName("getThing")
                .build())
        .build();

    assertOutput(type);
  }

  @Test
  public void testSimpleInterface() throws Exception {
    ClassMetadata type = ClassMetadata.builder()
        .packageName("com.bdl.auto.adapter")
        .category(ClassMetadata.Category.INTERFACE)
        .name("Simple")
        .addAbstractMethod(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setType("int")
                .setName("add")
                .addParameter(ParameterMetadata.of("int", "first"))
                .addParameter(ParameterMetadata.of("int", "second"))
                .build())
        .build();

    assertOutput(type);
  }

  @Test
  public void testParameterized() throws Exception {
    ClassMetadata type = ClassMetadata.builder()
        .packageName("com.bdl.auto.adapter")
        .category(ClassMetadata.Category.INTERFACE)
        .name("Parameterized")
        .addTypeParameter(TypeParameterMetadata.builder().setName("T").addBound("Foo").build())
        .addAbstractMethod(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setType("T")
                .setName("frozzle")
                .addParameter(ParameterMetadata.of("T", "input"))
                .build())
        .build();

    assertOutput(type);
  }

  @Test
  public void testHasAnImplementedMethod() throws Exception {
    MethodMetadata addMethod = MethodMetadata.builder()
        .setVisibility(Visibility.PUBLIC)
        .setType("int")
        .setName("add")
        .addParameter(ParameterMetadata.of("int", "first"))
        .addParameter(ParameterMetadata.of("int", "second"))
        .build();
    MethodMetadata subtractMethod = MethodMetadata.builder()
        .setVisibility(Visibility.PUBLIC)
        .setType("int")
        .setName("subtract")
        .addParameter(ParameterMetadata.of("int", "first"))
        .addParameter(ParameterMetadata.of("int", "second"))
        .build();
    ClassMetadata type = ClassMetadata.builder()
        .packageName("com.bdl.auto.adapter")
        .category(ClassMetadata.Category.CLASS)
        .name("Partial")
        .addAbstractMethod(addMethod)
        .addAbstractMethod(subtractMethod)
        .addImplementedMethod(addMethod)
        .build();

    assertOutput(type);
  }

  @Test
  public void testHasConstructors() throws Exception {
    ClassMetadata type = ClassMetadata.builder()
        .packageName("com.bdl.auto.adapter")
        .category(ClassMetadata.Category.CLASS)
        .name("Constructable")
        .addConstructor(ConstructorMetadata.builder()
            .visibility(Visibility.PUBLIC)
            .addParameter(ParameterMetadata.of("int", "arg1"))
            .addParameter(ParameterMetadata.of("String", "arg2"))
            .build())
        .addConstructor(ConstructorMetadata.builder()
            .visibility(Visibility.PACKAGE_LOCAL)
            .addParameter(ParameterMetadata.of("String", "arg1"))
            .build())
        .addAbstractMethod(MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setType("int")
            .setName("add")
            .addParameter(ParameterMetadata.of("int", "first"))
            .addParameter(ParameterMetadata.of("int", "second"))
            .build())
        .addAbstractMethod(MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setType("int")
            .setName("subtract")
            .addParameter(ParameterMetadata.of("int", "first"))
            .addParameter(ParameterMetadata.of("int", "second"))
            .build())
        .build();

    assertOutput(type);
  }

  private void assertOutput(ClassMetadata type) throws Exception {
    final Map<String, Writer> writerMap = Maps.newHashMap();

    ThrowingAdapterWriter writer = new ThrowingAdapterWriter(
        new Function<String, Writer>() {
          @Override
          public Writer apply(String input) {
            StringWriter writer = new StringWriter();
            writerMap.put(input + ".txt", writer);
            return writer;
          }
        });

    String key = String.format("%s.%s.txt", type.packageName(), type.decoratedName("Throwing"));
    writer.write(type);

    URL resource = getClass().getClassLoader().getResource(key);
    String file = Resources.toString(resource, Charsets.UTF_8);

    assertThat(writerMap.get(key).toString()).isEqualTo(file);
  }
}
