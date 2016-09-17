package com.bdl.auto.impl;

import com.google.common.base.Function;

import java.io.IOException;
import java.io.Writer;

/**
 * A class that writes out Auto-implementations.
 *
 * @author Ben Leitner
 */
class AutoImplWriter {

  private final Function<String, Writer> writerFunction;

  protected AutoImplWriter(Function<String, Writer> writerFunction) {
    this.writerFunction = writerFunction;
  }

  void write(ClassMetadata clazz) throws IOException {
    TypeMetadata type = clazz.type();
    Writer writer = writerFunction.apply(type.nameBuilder()
            .addPackagePrefix()
            .append("Auto_")
            .addNestingPrefix("_")
            .addSimpleName()
            .append("_")
            .append("Impl")
            .toString());

    writeClassOpening(writer, clazz);

    for (ConstructorMetadata constructor : clazz.getOrderedRequiredConstructors()) {
      writeConstructor(
          writer,
          type.nameBuilder()
              .append("Auto_")
              .addNestingPrefix("_")
              .addSimpleName()
              .append("_")
              .append("Impl")
              .toString(),
          constructor);
    }

    AutoImpl autoImpl = AnnotationUtil.autoImpl(clazz);
    for (MethodMetadata method : clazz.getOrderedRequiredMethods()) {
      writeMethod(writer, autoImpl, method.toBuilder().setIsAbstract(false).build());
    }
    writeClassClosing(writer);

    if (writer != null) {
      writer.close();
    }
  }

  private void writeClassOpening(Writer writer, ClassMetadata clazz)
      throws IOException {
    TypeMetadata type = clazz.type();
    writeLine(writer, "package %s;", type.packageName());
    writeLine(writer, "");
    writeLine(writer, "import javax.annotation.Generated;");
    writeLine(writer, "");
    writeLine(writer, "/** AutoImpl Generated class for %s. */",
        type.nameBuilder().addNestingPrefix().addSimpleName().toString());
    writeLine(writer, "@Generated(\"com.bdl.auto.impl.AutoImplProcessor\")");
    writeLine(writer, "public class %s %s %s {",
        type.nameBuilder()
            .append("Auto_")
            .addNestingPrefix("_")
            .addSimpleName()
            .append("_")
            .append("Impl")
            .addFullParams()
            .toString(),
        clazz.category() == ClassMetadata.Category.CLASS ? "extends" : "implements",
        type.nameBuilder().addNestingPrefix().addSimpleName().addSimpleParams().toString());
  }

  private void writeConstructor(Writer writer, String name, ConstructorMetadata constructor) throws IOException {
    writeLine(writer, "");
    writeLine(writer, "  %s {", constructor.toString(name));
    writeLine(writer, "    %s;", constructor.superCall());
    writeLine(writer, "  }");
  }

  private void writeMethod(Writer writer, AutoImpl autoImpl, MethodMetadata method) throws IOException {
    switch (optionForMethod(autoImpl, method)) {
      case THROW_EXCEPTION:
        writeThrowingMethod(writer, method);
        break;
      case RETURN_DEFAULT_VALUE:
        writeDefaultValueMethod(writer, method);
        break;
      default:
        throw new IllegalStateException(
            String.format("Could not determine implementation option for method %s", method.fullDescription()));
    }
  }

  private ImplOption optionForMethod(AutoImpl autoImpl, MethodMetadata method) {
    MethodImpl methodImpl = AnnotationUtil.methodImpl(method);
    if (methodImpl.value() != ImplOption.USE_PARENT) {
      return methodImpl.value();
    }

    ImplOption option = ImplOption.USE_PARENT;
    switch (method.type().kind()) {
      case NUMERIC:
        option = autoImpl.numericImpl();
        break;
      case BOOLEAN:
        option = autoImpl.booleanImpl();
        break;
      case STRING:
        option = autoImpl.stringImpl();
        break;
      case VOID:
        option = autoImpl.voidImpl();
        break;
      case OBJECT:
        option = autoImpl.objectImpl();
        break;
    }
    return option == ImplOption.USE_PARENT
        ? autoImpl.value()
        : option;
  }

  private void writeThrowingMethod(Writer writer, MethodMetadata method) throws IOException {
    writeLine(writer, "");
    writeLine(writer, "  @Override");
    writeLine(writer, "  %s {", method.fullDescription());
    writeLine(writer,
        "    throw new UnsupportedOperationException(\"The method \\\"%s\\\" is not supported in this implementation.\");",
        method.fullDescription());
    writeLine(writer, "  }");
  }


  private void writeDefaultValueMethod(Writer writer, MethodMetadata method) throws IOException {
    writeLine(writer, "");
    writeLine(writer, "  @Override");
    writeLine(writer, "  %s {", method.fullDescription());
    if (!method.type().name().equals("void")) {
      writeLine(writer, "    return %s;", getDefaultReturn(method.type()));
    }
    writeLine(writer, "  }");
  }

  protected static String getDefaultReturn(TypeMetadata type) {
    switch (type.kind()) {
      case NUMERIC:
        return "0";
      case BOOLEAN:
        return "false";
      case STRING:
        return "\"\"";
      default:
        return "null";
    }
  }

  private void writeClassClosing(Writer writer) throws IOException {
    writeLine(writer, "}");
  }

  protected static void writeLine(Writer writer, String template, Object... params) throws IOException {
    writer.write(String.format(template, params));
    writer.write("\n");
  }
}
