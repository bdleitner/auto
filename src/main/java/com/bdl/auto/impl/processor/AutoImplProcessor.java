package com.bdl.auto.impl.processor;

import com.google.common.base.Function;

import com.bdl.annotation.processing.model.ClassMetadata;
import com.bdl.auto.impl.AutoImpl;

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
 * Annotation Processor to generate AutoImpl classes.
 *
 * @author Ben Leitner
 */
@SupportedAnnotationTypes("com.bdl.auto.impl.AutoImpl")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AutoImplProcessor extends AbstractProcessor {

  private Messager messager;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    messager = processingEnv.getMessager();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (Element element : roundEnv.getElementsAnnotatedWith(AutoImpl.class)) {
      if (element.getKind() != ElementKind.CLASS
          && element.getKind() != ElementKind.INTERFACE) {
        messager.printMessage(
            Diagnostic.Kind.ERROR,
            "AutoImpl Annotation applied to an element that is not a class or interface.",
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
          String.format("AutoImpl annotation added to non-abstract class: %s", element));
      return;
    }
    ClassMetadata classMetadata = ClassMetadata.fromElement(element);

    try {
      JavaFileObjectWriterFunction writerFunction = new JavaFileObjectWriterFunction(processingEnv);

      AutoImplWriter writer = new AutoImplWriter(writerFunction);
      writer.write(classMetadata);
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
