package com.bdl.auto.adapter;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
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
public class DefaultValuesAdapterWriterTest {

  @Test
  public void testSimpleClass() throws Exception {
    TypeMetadata type = TypeMetadata.builder()
        .packageName("com.bdl.auto.adapter")
        .type(TypeMetadata.Type.CLASS)
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
                .setType("java.lang.String")
                .setName("repeat")
                .addParameter(ParameterMetadata.of("java.lang.String", "template"))
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
    TypeMetadata type = TypeMetadata.builder()
        .packageName("com.bdl.auto.adapter")
        .type(TypeMetadata.Type.INTERFACE)
        .name("SimpleInterface")
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
                .setType("void")
                .setName("modify")
                .addParameter(ParameterMetadata.of("java.lang.String", "input"))
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
    TypeMetadata type = TypeMetadata.builder()
        .packageName("com.bdl.auto.adapter")
        .type(TypeMetadata.Type.CLASS)
        .name("Partial")
        .addAbstractMethod(addMethod)
        .addAbstractMethod(subtractMethod)
        .addImplementedMethod(addMethod)
        .build();

    assertOutput(type);
  }

  @Test
  public void testHasConstructors() throws Exception {
    TypeMetadata type = TypeMetadata.builder()
        .packageName("com.bdl.auto.adapter")
        .type(TypeMetadata.Type.CLASS)
        .name("Constructable")
        .addConstructor(ConstructorMetadata.builder()
            .visibility(Visibility.PUBLIC)
            .addParameter(ParameterMetadata.of("int", "arg1"))
            .addParameter(ParameterMetadata.of("java.lang.String", "arg2"))
            .build())
        .addConstructor(ConstructorMetadata.builder()
            .visibility(Visibility.PACKAGE_LOCAL)
            .addParameter(ParameterMetadata.of("java.lang.String", "arg1"))
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

  @Test
  public void testNested() throws Exception {
    TypeMetadata type = TypeMetadata.builder()
        .packageName("com.bdl.auto.adapter")
        .type(TypeMetadata.Type.CLASS)
        .name("Inner")
        .nestInside("Outer")
        .nestInside("Super")
        .addConstructor(ConstructorMetadata.builder()
            .visibility(Visibility.PUBLIC)
            .addParameter(ParameterMetadata.of("int", "arg1"))
            .addParameter(ParameterMetadata.of("java.lang.String", "arg2"))
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

  private void assertOutput(TypeMetadata type) throws Exception {
    final Map<String, Writer> writerMap = Maps.newHashMap();

    DefaultValuesAdapterWriter writer = new DefaultValuesAdapterWriter(
        new Function<String, Writer>() {
          @Override
          public Writer apply(String input) {
            StringWriter writer = new StringWriter();
            writerMap.put(input + ".txt", writer);
            return writer;
          }
        });

    String key = String.format("%s.%s.txt", type.packageName(), type.decoratedName("DefaultValues"));
    writer.write(type);

    URL resource = getClass().getClassLoader().getResource(key);
    String file = Resources.toString(
        Preconditions.checkNotNull(resource, "Resource for %s could not be loaded.", key), Charsets.UTF_8);

    assertThat(writerMap.get(key).toString()).isEqualTo(file);
  }
}
