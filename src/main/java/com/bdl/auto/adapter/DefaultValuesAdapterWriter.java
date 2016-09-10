package com.bdl.auto.adapter;

import com.google.common.base.Function;

import java.io.IOException;
import java.io.Writer;

/**
 * A writer that writes out a Default-Values implementation of the abstract class / interface.
 * All methods not already implemented will return default values:
 * <ul>
 *   <li>All numeric types: 0</li>
 *   <li>Booleans: false</li>
 *   <li>Strings: ""</li>
 *   <li>Everything Else: null</li>
 * </ul>
 *
 * @author Ben Leitner
 */
class DefaultValuesAdapterWriter extends AbstractAdapterWriter {

  DefaultValuesAdapterWriter(Function<String, Writer> writerFunction) {
    super(writerFunction, "DefaultValues");
  }

  @Override
  protected void writeMethod(Writer writer, MethodMetadata method) throws IOException {
    writeLine(writer, "");
    writeLine(writer, "  @Override");
    writeLine(writer, "  %s {", method.fullDescription());
    if (!method.type().name().equals("void")) {
      writeLine(writer, "    return %s;", getDefaultReturn(method.type()));
    }
    writeLine(writer, "  }");
  }

  protected static String getDefaultReturn(TypeMetadata type) {
    switch (type.nameBuilder().addPackagePrefix().addNestingPrefix().addSimpleName().toString()) {
      case "java.lang.Integer":
      case "java.lang.Long":
      case "java.lang.Double":
      case "java.lang.Float":
      case "java.lang.Short":
      case "java.lang.Byte":
      case "java.lang.Character":
      case "Integer":
      case "Long":
      case "Double":
      case "Float":
      case "Short":
      case "Byte":
      case "Character":
      case "int":
      case "long":
      case "double":
      case "float":
      case "short":
      case "byte":
      case "char":
        return "0";
      case "String":
      case "java.lang.String":
        return "\"\"";
      case "Boolean":
      case "java.lang.Boolean":
      case "boolean":
        return "false";
      default:
        return "null";
    }
  }
}
