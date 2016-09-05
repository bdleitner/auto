package com.bdl.auto.adapter;

import com.google.common.collect.Sets;

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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Annotation Processor to generate AutoAdapter classes.
 *
 * @author Ben Leitner
 */
@SupportedAnnotationTypes("com.bdl.auto.adapter.AutoAdapter")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class AutoAdapterAnnotationProcessor extends AbstractProcessor {

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

  private TypeElement convert(TypeMirror type) {
    return ((TypeElement) ((DeclaredType) type).asElement());
  }

  private void processElement(TypeElement element) {
    Set<MethodMetadata> abstractExecutables = Sets.newHashSet();
    collectAllAbstractExecutables(element, abstractExecutables);

    Set<MethodMetadata> implementedExecutables = Sets.newHashSet();
    collectAllImplementedExecutables(element, implementedExecutables);

    abstractExecutables.removeAll(implementedExecutables);

    messager.printMessage(Diagnostic.Kind.NOTE, "");
    messager.printMessage(Diagnostic.Kind.NOTE, "");
    messager.printMessage(Diagnostic.Kind.NOTE, "Messages remaining to implement:");
    for (MethodMetadata executable : abstractExecutables) {
      messager.printMessage(Diagnostic.Kind.NOTE, "  " + executable.toString());
    }
    messager.printMessage(Diagnostic.Kind.NOTE, "");
    messager.printMessage(Diagnostic.Kind.NOTE, "");
  }

  private void collectAllAbstractExecutables(TypeElement type, Set<MethodMetadata> executables) {
    messager.printMessage(Diagnostic.Kind.NOTE, String.format("Processing type %s for abstract methods.", type));
    if (!type.getModifiers().contains(Modifier.ABSTRACT)) {
      messager.printMessage(Diagnostic.Kind.NOTE, String.format("Type %s is not abstract, skipping.", type));
      return;
    }

    for (Element enclosed : type.getEnclosedElements()) {
      if (enclosed.getKind() != ElementKind.METHOD) {
        continue;
      }

      ExecutableElement executable = ((ExecutableElement) enclosed);
      if (executable.getModifiers().contains(Modifier.ABSTRACT)) {
        messager.printMessage(Diagnostic.Kind.NOTE, String.format("Found abstract method %s in %s", executable, type));
        executables.add(MethodMetadata.fromMethod(executable));
      }
    }

    for (TypeMirror anInterface : type.getInterfaces()) {
      collectAllAbstractExecutables(convert(anInterface), executables);
    }

    TypeMirror superclass = type.getSuperclass();
    if (superclass.getKind() == TypeKind.NONE) {
      return;
    }
    collectAllAbstractExecutables(convert(superclass), executables);
  }

  private void collectAllImplementedExecutables(TypeElement type, Set<MethodMetadata> executables) {
    messager.printMessage(Diagnostic.Kind.NOTE, String.format("Processing type %s for implemented methods.", type));

    for (Element enclosed : type.getEnclosedElements()) {
      if (enclosed.getKind() != ElementKind.METHOD) {
        continue;
      }

      ExecutableElement executable = ((ExecutableElement) enclosed);
      if (!executable.getModifiers().contains(Modifier.ABSTRACT)) {
        messager.printMessage(Diagnostic.Kind.NOTE,
            String.format("Found implemented method %s in %s", executable, type));
        executables.add(MethodMetadata.fromMethod(executable));
      }
    }

    TypeMirror superclass = type.getSuperclass();
    if (superclass.getKind() == TypeKind.NONE) {
      return;
    }
    collectAllImplementedExecutables(convert(superclass), executables);
  }
}
