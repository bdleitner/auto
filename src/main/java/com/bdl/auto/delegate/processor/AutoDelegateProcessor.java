package com.bdl.auto.delegate.processor;

import com.bdl.annotation.processing.model.ClassMetadata;
import com.bdl.annotation.processing.model.ConstructorMetadata;
import com.bdl.annotation.processing.model.FieldMetadata;
import com.bdl.annotation.processing.model.TypeMetadata;
import com.bdl.annotation.processing.model.Visibility;
import com.bdl.auto.impl.AutoImpl;
import com.google.common.base.Function;
import com.google.common.base.Throwables;

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
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

/**
 * Annotation Processor to generate AutoDelegate classes.
 *
 * @author Ben Leitner
 */
@SupportedAnnotationTypes("com.bdl.auto.delegate.AutoDelegate")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AutoDelegateProcessor extends AbstractProcessor {

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
          || !element.getModifiers().contains(Modifier.ABSTRACT)) {
        messager.printMessage(
            Diagnostic.Kind.ERROR,
            "AutoDelegate Annotation applied to an element that is not an abstract class.",
            element);
        return true;
      }
      processElement((TypeElement) element);
    }

    return true;
  }

  private void processElement(TypeElement element) {
    ClassMetadata classMetadata = ClassMetadata.fromElement(element);

    if (!validate(classMetadata)) {
      return;
    }

    try {
      JavaFileObjectWriterFunction writerFunction = new JavaFileObjectWriterFunction(processingEnv);

      AutoDelegateWriter writer = new AutoDelegateWriter(writerFunction);
      writer.write(classMetadata);
    } catch (Exception ex) {
      messager.printMessage(
          Diagnostic.Kind.ERROR,
          "Error in AutoDelegate Processor\n"
              + ex.getMessage()
              + "\n"
              + Throwables.getStackTraceAsString(ex));
    }
  }

  /**
   * Ensures that the class meets the requirements for being auto-delegated.
   *
   * <ul>
   * <li>Must have a single inheritance, either implementing a single interface or extending a
   *     single class.
   * <li>Must have a protected final field of the same type as the inherited type and named
   *     "delegate".
   * <li>Every constructor must have a parameter of the inherited type as the first argument.
   * </ul>
   */
  private boolean validate(ClassMetadata classMetadata) {
    if (classMetadata.inheritances().size() != 1) {
      messager.printMessage(
          Diagnostic.Kind.ERROR,
          String.format(
              "Class %s does not have a single inheritance."
                  + "  AutoDelegate classes must implement a single interface or extend a single class.",
              classMetadata));
      return false;
    }
    TypeMetadata inheritedType =
        classMetadata.inheritances().stream().findAny().get().classMetadata().type();

    if (classMetadata.fields().stream().noneMatch(field -> fieldMatches(field, inheritedType))) {
      messager.printMessage(
          Diagnostic.Kind.ERROR,
          String.format(
              "Class %s does not have a \"delegate\" field matching the inheritance type %s.",
              classMetadata.type().name(), inheritedType));
      return false;
    }

    for (ConstructorMetadata constructor : classMetadata.constructors()) {
      if (constructor.parameters().size() < 1
          || !constructor.parameters().get(0).type().equals(inheritedType)) {
        messager.printMessage(
            Diagnostic.Kind.ERROR,
            String.format(
                "Class %s's constructor %s does not have a first parameter matching the inheritance type %s.",
                classMetadata.type().name(), constructor, inheritedType));
        return false;
      }
    }
    return true;
  }

  private boolean fieldMatches(FieldMetadata field, TypeMetadata inheritedType) {
    return field.name().equals("delegate")
        && field.modifiers().isFinal()
        && field.modifiers().visibility() == Visibility.PROTECTED
        && field.type().equals(inheritedType);
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
