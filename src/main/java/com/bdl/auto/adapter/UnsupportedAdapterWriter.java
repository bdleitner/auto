package com.bdl.auto.adapter;

import com.google.common.base.Function;

import java.io.IOException;
import java.io.Writer;

/**
 * A writer that writes out a no-op implementation of the abstract class / interface.
 * All methods not already implemented will throw UnsupportedOperationException.
 *
 * @author Ben Leitner
 */
class UnsupportedAdapterWriter extends AbstractAdapterWriter {

  UnsupportedAdapterWriter(Function<String, Writer> writerFunction) {
    super(writerFunction, "Unsupported");
  }

  @Override
  protected void writeMethod(Writer writer, MethodMetadata method) throws IOException {
    writeLine(writer, "");
    writeLine(writer, "  @Override");
    writeLine(writer, "  %s {", method);
    if (!method.type().equals("void")) {
      writeLine(writer,
          "    throw new UnsupportedOperationException(\"The method \\\"%s\\\" is not supported in this implementation.\");",
          method);
    }
    writeLine(writer, "  }");
  }
}
