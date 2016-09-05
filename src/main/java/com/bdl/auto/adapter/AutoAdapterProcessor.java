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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
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

  private TypeElement convert(TypeMirror type) {
    return ((TypeElement) ((DeclaredType) type).asElement());
  }

  private void processElement(TypeElement element) {
    if (!element.getModifiers().contains(Modifier.ABSTRACT)) {
      messager.printMessage(Diagnostic.Kind.ERROR,
          String.format("AutoAdapter annotation added to non-abstract class: %s", element));
      return;
    }
    TypeMetadata.Builder typeBuilder = TypeMetadata.builder();
    collectBasicMetadata(element, typeBuilder);
    collectConstructors(element, typeBuilder);

    collectAllAbstractExecutables(element, typeBuilder);
    collectAllImplementedExecutables(element, typeBuilder);

    TypeMetadata typeMetadata = typeBuilder.build();

    try {
      DefaultValuesAdapterWriter notOpWriter = new DefaultValuesAdapterWriter(new JavaFileObjectWriterFunction(processingEnv));
      notOpWriter.write(typeMetadata);

      ThrowingAdapterWriter throwingWriter = new ThrowingAdapterWriter(
          new JavaFileObjectWriterFunction(processingEnv));
      throwingWriter.write(typeMetadata);
    } catch (Exception ex) {
      messager.printMessage(
          Diagnostic.Kind.ERROR,
          ex.getMessage());
    }
  }

  private void collectBasicMetadata(Element element, TypeMetadata.Builder typeBuilder) {
    typeBuilder.name(element.getSimpleName().toString());
    typeBuilder.type(TypeMetadata.Type.forKind(element.getKind()));

    element = element.getEnclosingElement();
    while (element.getKind() != ElementKind.PACKAGE) {
      typeBuilder.nestInside(element.getSimpleName().toString());
      element = element.getEnclosingElement();
    }

    typeBuilder.packageName(((PackageElement) element).getQualifiedName().toString());
  }

  private void collectConstructors(Element element, TypeMetadata.Builder typeBuilder) {
    for (Element enclosed : element.getEnclosedElements()) {
      if (enclosed.getKind() != ElementKind.CONSTRUCTOR) {
        continue;
      }
      ConstructorMetadata constructor = ConstructorMetadata.fromConstructor(enclosed);
      typeBuilder.addConstructor(constructor);
    }
  }

  private void collectAllAbstractExecutables(TypeElement type, TypeMetadata.Builder typeBuilder) {
    if (!type.getModifiers().contains(Modifier.ABSTRACT)) {
      return;
    }

    for (Element enclosed : type.getEnclosedElements()) {
      if (enclosed.getKind() != ElementKind.METHOD) {
        continue;
      }

      ExecutableElement executable = ((ExecutableElement) enclosed);
      if (executable.getModifiers().contains(Modifier.ABSTRACT)) {
        MethodMetadata method = MethodMetadata.fromMethod(executable);
        typeBuilder.addAbstractMethod(method);
      }
    }

    for (TypeMirror anInterface : type.getInterfaces()) {
      collectAllAbstractExecutables(convert(anInterface), typeBuilder);
    }

    TypeMirror superclass = type.getSuperclass();
    if (superclass.getKind() == TypeKind.NONE) {
      return;
    }
    collectAllAbstractExecutables(convert(superclass), typeBuilder);
  }

  private void collectAllImplementedExecutables(TypeElement type, TypeMetadata.Builder typeBuilder) {
    if (type.getQualifiedName().toString().equals("java.lang.Object")) {
      return;
    }

    for (Element enclosed : type.getEnclosedElements()) {
      if (enclosed.getKind() != ElementKind.METHOD) {
        continue;
      }

      ExecutableElement executable = ((ExecutableElement) enclosed);
      if (!executable.getModifiers().contains(Modifier.ABSTRACT)) {
        MethodMetadata method = MethodMetadata.fromMethod(executable);
        typeBuilder.addImplementedMethod(method);
      }
    }

    TypeMirror superclass = type.getSuperclass();
    if (superclass.getKind() == TypeKind.NONE) {
      return;
    }
    collectAllImplementedExecutables(convert(superclass), typeBuilder);
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
