package com.bdl.auto.adapter;

import com.google.common.base.Function;

import java.io.IOException;
import java.io.Writer;

/**
 * A writer that writes out a no-op implementation of the classes.
 *
 * @author Ben Leitner
 */
class NoOpAdapterWriter {

  private final Function<String, Writer> writerFunction;

  NoOpAdapterWriter(Function<String, Writer> writerFunction) {
    this.writerFunction = writerFunction;
  }

  void write(TypeMetadata type) throws IOException {
    Writer writer = writerFunction.apply(
        String.format("%s.%s", type.packageName(), type.decoratedName("NoOp")));

    writeClassOpening(writer, type);

    for (ConstructorMetadata constructor : type.orderedRequiredConstructors()) {
      writeConstructor(writer, type.decoratedName("NoOp"), constructor);
    }

    for (MethodMetadata method : type.orderedRequiredMethods()) {
      writeMethod(writer, method);
    }
    writeClassClosing(writer);

    writer.close();
  }

  private void writeClassOpening(Writer writer, TypeMetadata type)
      throws IOException {
    writeLine(writer, "package %s;", type.packageName());
    writeLine(writer, "");
    // TODO: imports
    writeLine(writer, "/** No-Op AutoAdapter Generated class for %s. */", type.name());
    // TODO: what if the base class is an interface?
    writeLine(writer, "public class %s %s %s {",
        type.decoratedName("NoOp"), type.type() == TypeMetadata.Type.CLASS ? "extends" : "implements", type.name());
  }

  private void writeConstructor(Writer writer, String name, ConstructorMetadata constructor) throws IOException {
    writeLine(writer, "");
    writeLine(writer, "  %s {", constructor.toString(name));
    writeLine(writer, "    %s;", constructor.superCall());
    writeLine(writer, "  }");
  }

  private void writeMethod(Writer writer, MethodMetadata method) throws IOException {
    writeLine(writer, "");
    writeLine(writer, "  @Override");
    writeLine(writer, "  %s {", method);
    if (!method.type().equals("void")) {
      writeLine(writer, "    return %s;", getDefaultReturn(method.type()));
    }
    writeLine(writer, "  }");
  }

  private String getDefaultReturn(String type) {
    switch (type) {
      case "java.lang.Integer":
      case "java.lang.Long":
      case "java.lang.Double":
      case "java.lang.Float":
      case "java.lang.Short":
      case "java.lang.Byte":
      case "java.lang.Character":
      case "int":
      case "long":
      case "double":
      case "float":
      case "short":
      case "byte":
      case "char":
        return "0";
      case "java.lang.String":
        return "\"\"";
      case "java.lang.Boolean":
      case "boolean":
        return "false";
      default:
        return "null";
    }
  }

  private void writeClassClosing(Writer writer) throws IOException {
    writeLine(writer, "}");
  }

  private void writeLine(Writer writer, String template, Object... params) throws IOException {
    writer.write(String.format(template, params));
    writer.write("\n");
  }
}
