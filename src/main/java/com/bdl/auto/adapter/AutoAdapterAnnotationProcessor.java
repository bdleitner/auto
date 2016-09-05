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
    Set<ExecutableElement> abstractExecutables = Sets.newHashSet();

    collectAllAbstractExecutablesFromInterface(element, abstractExecutables);
    collectAllAbstractExecutablesFromClasses(convert(element.getSuperclass()), abstractExecutables);
  }

  private void collectAllAbstractExecutablesFromInterface(TypeElement type, Set<ExecutableElement> executables) {
    for (Element enclosed : type.getEnclosedElements()) {
      if (enclosed.getKind() != ElementKind.METHOD) {
        continue;
      }

      ExecutableElement executable = ((ExecutableElement) enclosed);
      if (executable.getModifiers().contains(Modifier.ABSTRACT)) {
        executables.add(executable);
      }
    }

    for (TypeMirror anInterface : type.getInterfaces()) {
      collectAllAbstractExecutablesFromInterface(convert(anInterface), executables);
    }
  }

  private void collectAllAbstractExecutablesFromClasses(TypeElement clazz, Set<ExecutableElement> abstractExecutables) {
    if (clazz.getModifiers().contains(Modifier.ABSTRACT)) {
      collectAllAbstractExecutablesFromInterface(clazz, abstractExecutables);
    }

    // TODO: get the next superclass and recurse.  How do we figure out when we're done?
  }
}
