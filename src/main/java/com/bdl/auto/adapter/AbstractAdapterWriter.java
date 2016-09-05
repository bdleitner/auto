package com.bdl.auto.adapter;

import com.google.common.base.Function;

import java.io.IOException;
import java.io.Writer;

/**
 * Abstract base class for Adapter Writers.
 *
 * @author Ben Leitner
 */
abstract class AbstractAdapterWriter {

  private final Function<String, Writer> writerFunction;
  private final String suffix;

  protected AbstractAdapterWriter(Function<String, Writer> writerFunction, String suffix) {
    this.writerFunction = writerFunction;
    this.suffix = suffix;
  }

  void write(TypeMetadata type) throws IOException {
    Writer writer = writerFunction.apply(
        String.format("%s.%s", type.packageName(), type.decoratedName(suffix)));

    writeClassOpening(writer, type);

    for (ConstructorMetadata constructor : type.orderedRequiredConstructors()) {
      writeConstructor(writer, type.decoratedName(suffix), constructor);
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
    writeLine(writer, "/** %s AutoAdapter Generated class for %s. */", suffix, type.nestedClassName());
    writeLine(writer, "public class %s %s %s {",
        type.decoratedName(suffix),
        type.type() == TypeMetadata.Type.CLASS ? "extends" : "implements",
        type.nestedClassName());
  }

  private void writeConstructor(Writer writer, String name, ConstructorMetadata constructor) throws IOException {
    writeLine(writer, "");
    writeLine(writer, "  %s {", constructor.toString(name));
    writeLine(writer, "    %s;", constructor.superCall());
    writeLine(writer, "  }");
  }

  protected abstract void writeMethod(Writer writer, MethodMetadata method) throws IOException;

  private void writeClassClosing(Writer writer) throws IOException {
    writeLine(writer, "}");
  }

  protected static void writeLine(Writer writer, String template, Object... params) throws IOException {
    writer.write(String.format(template, params));
    writer.write("\n");
  }
}
