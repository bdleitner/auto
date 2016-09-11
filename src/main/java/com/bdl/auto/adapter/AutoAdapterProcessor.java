package com.bdl.auto.adapter;

import com.google.common.base.Function;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Annotation Processor to generate AutoAdapter classes.
 *
 * @author Ben Leitner
 */
@SupportedAnnotationTypes("com.bdl.auto.adapter.AutoAdapter")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class AutoAdapterProcessor extends AbstractProcessor {

  private Messager messager;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    messager = processingEnv.getMessager();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (Element element : roundEnv.getElementsAnnotatedWith(AutoAdapter.class)) {
      if (element.getKind() != ElementKind.CLASS
          && element.getKind() != ElementKind.INTERFACE) {
        messager.printMessage(
            Diagnostic.Kind.ERROR,
            "AutoAdapter Annotation applied to an element that is not a class or interface.",
            element);
        return true;
      }
      processElement((TypeElement) element);
    }

    return true;
  }

  private void processElement(TypeElement element) {
    if (!element.getModifiers().contains(Modifier.ABSTRACT)) {
      messager.printMessage(Diagnostic.Kind.ERROR,
          String.format("AutoAdapter annotation added to non-abstract class: %s", element));
      return;
    }
    ClassMetadata classMetadata = ClassMetadata.fromElement(element);

    try {
      JavaFileObjectWriterFunction writerFunction = new JavaFileObjectWriterFunction(processingEnv);

      DefaultValuesAdapterWriter notOpWriter = new DefaultValuesAdapterWriter(writerFunction);
      notOpWriter.write(classMetadata);

      ThrowingAdapterWriter throwingWriter = new ThrowingAdapterWriter(writerFunction);
      throwingWriter.write(classMetadata);
    } catch (Exception ex) {
      messager.printMessage(
          Diagnostic.Kind.ERROR,
          ex.getMessage());
    }
  }

  private static class JavaFileObjectWriterFunction implements Function<String, Writer> {

    private final ProcessingEnvironment env;

    private JavaFileObjectWriterFunction(ProcessingEnvironment env) {
      this.env = env;
    }

    @Override
    public Writer apply(String input) {
      try {
        JavaFileObject jfo = env.getFiler().createSourceFile(input);
        return jfo.openWriter();
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }
  }
}
