package com.bdl.auto.delegate.processor;

import com.bdl.annotation.processing.model.ClassMetadata;
import com.bdl.annotation.processing.model.ConstructorMetadata;
import com.bdl.annotation.processing.model.Imports;
import com.bdl.annotation.processing.model.MethodMetadata;
import com.bdl.annotation.processing.model.ParameterMetadata;
import com.bdl.annotation.processing.model.TypeMetadata;
import com.bdl.annotation.processing.model.Visibility;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Generated;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class that writes out Auto-implementations.
 *
 * @author Ben Leitner
 */
class AutoDelegateWriter {

  private final Function<String, Writer> writerFunction;

  protected AutoDelegateWriter(Function<String, Writer> writerFunction) {
    this.writerFunction = writerFunction;
  }

  void write(ClassMetadata clazz) throws IOException {
    TypeMetadata type = clazz.type();

    Writer writer =
        writerFunction.apply(
            type.packagePrefix() + "Auto_" + type.nestingPrefix("_") + type.name() + "_Delegate");

    ImmutableSet.Builder<TypeMetadata> types = ImmutableSet.builder();
    types.add(clazz.type());
    types.add(TypeMetadata.from(Generated.class));
    List<MethodMetadata> methods =
        clazz
            .getAllMethods()
            .stream()
            .filter(method -> method.modifiers().isAbstract())
            .sorted()
            .collect(Collectors.toList());
    for (MethodMetadata method : methods) {
      types.addAll(method.getAllTypes());
    }

    List<ConstructorMetadata> constructors =
        clazz
            .constructors()
            .stream()
            .filter((constructorMetadata) -> constructorMetadata.visibility() != Visibility.PRIVATE)
            .sorted()
            .collect(Collectors.toList());

    for (ConstructorMetadata constructor : constructors) {
      types.addAll(constructor.getAllTypes());
    }
    Imports imports = Imports.create(clazz.type().packageName(), types.build());
    writeClassOpening(writer, clazz, imports);

    for (ConstructorMetadata constructor : constructors) {
      writeConstructor(writer, imports, constructor);
    }

    for (MethodMetadata method : methods) {
      writeMethod(writer, imports, method.asConcrete());
    }
    writeClassClosing(writer);

    if (writer != null) {
      writer.close();
    }
  }

  private void writeClassOpening(Writer writer, ClassMetadata clazz, Imports imports)
      throws IOException {
    TypeMetadata type = clazz.type();
    writeLine(writer, "package %s;", type.packageName());
    writeLine(writer, "");
    for (String imp : imports.getImports()) {
      writeLine(writer, "import %s;", imp);
    }
    writeLine(writer, "");
    writeLine(
        writer, "/** AutoDelegate Generated class for %s. */", type.nestingPrefix() + type.name());
    writeLine(writer, "@Generated(\"com.bdl.auto.delegate.processor.AutoDelegateProcessor\")");
    writeLine(
        writer,
        "class Auto_%s%s_Delegate%s extends %s {",
        type.nestingPrefix("_"),
        type.name(),
        type.params().isEmpty()
            ? ""
            : "<"
                + type.params()
                    .stream()
                    .map((param) -> param.toString(imports, true))
                    .collect(Collectors.joining(", "))
                + ">",
        type.toString(imports));
  }

  private void writeConstructor(Writer writer, Imports imports, ConstructorMetadata constructor)
      throws IOException {
    writeLine(writer, "");
    writeLine(
        writer,
        "  %sAuto_%s%s_Delegate(%s) {",
        constructor.visibility().prefix(),
        constructor.type().nestingPrefix("_"),
        constructor.type().name(),
        constructor
            .parameters()
            .stream()
            .map((param) -> param.toString(imports))
            .collect(Collectors.joining(", ")));
    writeLine(
        writer,
        "    super(%s);",
        constructor
            .parameters()
            .stream()
            .map(ParameterMetadata::name)
            .collect(Collectors.joining(", ")));
    writeLine(writer, "  }");
  }

  private void writeMethod(Writer writer, Imports imports, MethodMetadata method)
      throws IOException {
    writeLine(writer, "");
    writeLine(writer, "  @Override");
    writeLine(writer, "  %s {", method.toString(imports));
    writeLine(writer, "    %sdelegate.%s(%s);",
        method.type().name().equals("void") ? "" : "return ",
        method.name(),
        method.parameters().stream().map(ParameterMetadata::name).collect(Collectors.joining(", ")));
    writeLine(writer, "  }");
  }

  private void writeClassClosing(Writer writer) throws IOException {
    writeLine(writer, "}");
  }

  protected static void writeLine(Writer writer, String template, Object... params)
      throws IOException {
    writer.write(String.format(template, params));
    writer.write("\n");
  }
}
