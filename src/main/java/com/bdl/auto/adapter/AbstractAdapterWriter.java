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

  void write(ClassMetadata clazz) throws IOException {
    TypeMetadata type = clazz.type();
    Writer writer = writerFunction.apply(type.nameBuilder()
            .addPackagePrefix()
            .append("AutoAdapter_")
            .addDecoratedNamePrefix()
            .addSimpleName()
            .append("_")
            .append(suffix)
            .toString());

    writeClassOpening(writer, clazz);

    for (ConstructorMetadata constructor : clazz.getOrderedRequiredConstructors()) {
      writeConstructor(writer, clazz.decoratedName(suffix), constructor);
    }

    for (MethodMetadata method : clazz.getOrderedRequiredMethods()) {
      writeMethod(writer, method.toBuilder().setIsAbstract(false).build());
    }
    writeClassClosing(writer);

    writer.close();
  }

  private void writeClassOpening(Writer writer, ClassMetadata clazz)
      throws IOException {
    TypeMetadata type = clazz.type();
    writeLine(writer, "package %s;", type.packageName());
    writeLine(writer, "");
    writeLine(writer, "import javax.annotation.Generated;");
    writeLine(writer, "");
    writeLine(writer, "/** %s AutoAdapter Generated class for %s. */", suffix,
        type.nameBuilder().addNestingPrefix().addSimpleName().toString());
    writeLine(writer, "@Generated(\"com.bdl.auto.adapter.AutoAdapterProcessor\")");
    writeLine(writer, "public class %s %s %s {",
        type.nameBuilder().append("AutoAdapter_").addDecoratedNamePrefix().addSimpleName().append("_").append(suffix).addFullParams(),
        clazz.category() == ClassMetadata.Category.CLASS ? "extends" : "implements",
        type.nameBuilder().addNestingPrefix().addSimpleName().addSimpleParams().toString());
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
