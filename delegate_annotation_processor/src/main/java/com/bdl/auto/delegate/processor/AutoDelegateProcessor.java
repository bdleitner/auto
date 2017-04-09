package com.bdl.auto.delegate.processor;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;

import com.bdl.annotation.processing.model.ClassMetadata;
import com.bdl.annotation.processing.model.ConstructorMetadata;
import com.bdl.annotation.processing.model.FieldMetadata;
import com.bdl.annotation.processing.model.InheritanceMetadata;
import com.bdl.annotation.processing.model.TypeMetadata;
import com.bdl.annotation.processing.model.Visibility;
import com.bdl.auto.delegate.AutoDelegate;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.Nullable;
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
 * Annotation Processor to generate AutoDelegate classes.
 *
 * @author Ben Leitner
 */
@SupportedAnnotationTypes("com.bdl.auto.delegate.AutoDelegate")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class AutoDelegateProcessor extends AbstractProcessor {

  private Messager messager;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    messager = processingEnv.getMessager();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (Element element : roundEnv.getElementsAnnotatedWith(AutoDelegate.class)) {
      if (element.getKind() != ElementKind.CLASS
          || !element.getModifiers().contains(Modifier.ABSTRACT)) {
        messager.printMessage(
            Diagnostic.Kind.ERROR,
            String.format(
                "AutoDelegate Annotation applied element %s, which is not an abstract class.",
                element),
            element);
        return true;
      }
      processElement((TypeElement) element);
    }

    return true;
  }

  private void processElement(final TypeElement element) {
    ClassMetadata classMetadata = ClassMetadata.fromElement(element);

    if (!validate(classMetadata)) {
      return;
    }

    try {
      JavaFileObjectWriterFunction writerFunction = new JavaFileObjectWriterFunction(processingEnv);

      AutoDelegateWriter writer = new AutoDelegateWriter(writerFunction,
          new AutoDelegateWriter.Recorder() {
            @Override
            public void record(String s) {
              messager.printMessage(Diagnostic.Kind.NOTE, s, element);
            }
          });
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
   *     "com.bdl.auto.delegate".
   * <li>Every constructor must have a parameter of the inherited type as the first argument.
   * </ul>
   */
  private boolean validate(ClassMetadata classMetadata) {
    Set<InheritanceMetadata> inheritances = FluentIterable.from(classMetadata.inheritances())
        .filter(new Predicate<InheritanceMetadata>() {
          @Override
          public boolean apply(@Nullable InheritanceMetadata input) {
            return !input.classMetadata().fullyQualifiedPathName().equals("java.lang.Object");
          }
        }).toSet();
    if (inheritances.size() != 1) {
      messager.printMessage(
          Diagnostic.Kind.ERROR,
          String.format(
              "Class %s does not have a single inheritance. "
                  + "AutoDelegate classes must implement a single interface"
                  + " or extend a single class: was %s",
              classMetadata.type().name(),
              Joiner.on(", ").join(FluentIterable.from(inheritances).transform(new Function<InheritanceMetadata, String>() {
                @Nullable
                @Override
                public String apply(@Nullable InheritanceMetadata input) {
                  return input.classMetadata().fullyQualifiedPathName();
                }
              }))));
      return false;
    }
    final TypeMetadata inheritedType = FluentIterable.from(inheritances).first().get().classMetadata().type();

    if (noDelegateField(classMetadata, inheritedType)) {
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

  private boolean noDelegateField(ClassMetadata classMetadata, final TypeMetadata inheritedType) {
    return !FluentIterable.from(classMetadata.fields()).anyMatch(new Predicate<FieldMetadata>() {
      @Override
      public boolean apply(@Nullable FieldMetadata input) {
        return fieldMatches(input, inheritedType);
      }
    });
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
