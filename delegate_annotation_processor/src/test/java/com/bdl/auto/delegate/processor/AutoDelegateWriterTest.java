package com.bdl.auto.delegate.processor;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.google.testing.compile.CompilationRule;

import com.bdl.annotation.processing.model.ClassMetadata;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Map;

import javax.lang.model.util.Elements;

/**
 * Tests for the {@linkplain AutoDelegateWriter} class.
 *
 * @author Ben Leitner
 */
@RunWith(JUnit4.class)
public class AutoDelegateWriterTest {

  @Rule public final CompilationRule compilation = new CompilationRule();

  private Elements elements;

  @Before
  public void before() {
    elements = compilation.getElements();
  }

  @Test
  public void testSimple() throws Exception {
    ClassMetadata clazz =
        ClassMetadata.fromElement(
            elements.getTypeElement("com.bdl.auto.delegate.processor.Simple"));
    assertOutput(clazz);
  }

  @Test
  public void testInnerSimple() throws Exception {
    ClassMetadata clazz =
        ClassMetadata.fromElement(
            elements.getTypeElement("com.bdl.auto.delegate.processor.Simple.InnerSimple"));
    assertOutput(clazz);
  }

  @Test
  public void testParameterized() throws Exception {
    ClassMetadata clazz =
        ClassMetadata.fromElement(
            elements.getTypeElement("com.bdl.auto.delegate.processor.Parameterized"));
    assertOutput(clazz);
  }

  private void assertOutput(ClassMetadata type) throws Exception {
    final Map<String, Writer> writerMap = Maps.newHashMap();

    AutoDelegateWriter writer =
        new AutoDelegateWriter(
            new Function<String, Writer>() {
              @Override
              public Writer apply(String input) {
                StringWriter writer = new StringWriter();
                writerMap.put(input + ".txt", writer);
                return writer;
              }
            }, new AutoDelegateWriter.Recorder() {
          @Override
          public void record(String s) {
            // Ignore
          }
        });

    String key =
        String.format(
            "%s.Auto_%s%s_Delegate.txt",
            type.type().packageName(), type.type().nestingPrefix("_"), type.type().name());
    writer.write(type);

    URL resource = getClass().getClassLoader().getResource(key);
    String file = Resources.toString(resource, Charsets.UTF_8);

    assertThat(writerMap.get(key).toString()).isEqualTo(file);
  }
}
