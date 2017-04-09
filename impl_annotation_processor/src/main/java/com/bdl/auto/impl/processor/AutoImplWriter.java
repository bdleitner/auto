package com.bdl.auto.impl.processor;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;

import com.bdl.annotation.processing.model.ClassMetadata;
import com.bdl.annotation.processing.model.ConstructorMetadata;
import com.bdl.annotation.processing.model.Imports;
import com.bdl.annotation.processing.model.MethodMetadata;
import com.bdl.annotation.processing.model.ParameterMetadata;
import com.bdl.annotation.processing.model.TypeMetadata;
import com.bdl.annotation.processing.model.Visibility;
import com.bdl.auto.impl.AutoImpl;
import com.bdl.auto.impl.ImplOption;
import com.bdl.auto.impl.MethodImpl;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.annotation.Generated;
import javax.annotation.Nullable;

/**
 * A class that writes out Auto-implementations.
 *
 * @author Ben Leitner
 */
class AutoImplWriter {

  interface Recorder {
    void record(String s);
  }
  
  private final Function<String, Writer> writerFunction;
  private final Recorder log;

  protected AutoImplWriter(Function<String, Writer> writerFunction, Recorder log) {
    this.writerFunction = writerFunction;
    this.log = log;
  }

  void write(ClassMetadata clazz) throws IOException {
    TypeMetadata type = clazz.type();
    log.record(String.format("Writing AutoImpl class for %s", type.fullyQualifiedPathName()));
    Writer writer = writerFunction.apply(
        type.packagePrefix() +
            "Auto_" +
            type.nestingPrefix("_") +
            type.name() +
            "_Impl");

    ImmutableSet.Builder<TypeMetadata> types = ImmutableSet.builder();
    types.add(clazz.type());
    types.add(TypeMetadata.from(Generated.class));
    final List<MethodMetadata> methods = FluentIterable.from(clazz.getAllMethods())
        .filter(new Predicate<MethodMetadata>() {
          @Override
          public boolean apply(@Nullable MethodMetadata input) {
            return input.modifiers().isAbstract();
          }
        }).toSortedList(Ordering.natural());

    for (MethodMetadata method : methods) {
      types.addAll(method.getAllTypes());
    }

    List<ConstructorMetadata> constructors = FluentIterable.from(clazz.constructors())
        .filter(new Predicate<ConstructorMetadata>() {
          @Override
          public boolean apply(@Nullable ConstructorMetadata input) {
            return input.visibility() != Visibility.PRIVATE;
          }
        }).toSortedList(Ordering.<ConstructorMetadata>natural());

    for (ConstructorMetadata constructor : constructors) {
      types.addAll(constructor.getAllTypes());
    }
    Imports imports = Imports.create(clazz.type().packageName(), types.build());
    writeClassOpening(writer, clazz, imports);

    for (ConstructorMetadata constructor : constructors) {
      writeConstructor(
          writer,
          imports,
          constructor);
    }

    AutoImpl autoImpl = AnnotationUtil.autoImpl(clazz);

    for (MethodMetadata method : methods) {
      writeMethod(writer, autoImpl, imports, method.asConcrete());
    }
    writeClassClosing(writer);

    if (writer != null) {
      writer.close();
    }
  }

  private void writeClassOpening(Writer writer, ClassMetadata clazz, final Imports imports) throws IOException {
    TypeMetadata type = clazz.type();
    writeLine(writer, "package %s;", type.packageName());
    writeLine(writer, "");
    for (String imp : imports.getImports()) {
      writeLine(writer, "import %s;", imp);
    }
    writeLine(writer, "");
    writeLine(writer, "/** AutoImpl Generated class for %s. */", type.nestingPrefix() + type.name());
    writeLine(writer, "@Generated(\"com.bdl.auto.impl.processor.AutoImplProcessor\")");
    writeLine(writer, "public class Auto_%s%s_Impl%s %s %s {",
        type.nestingPrefix("_"),
        type.name(),
        type.params().isEmpty()
            ? ""
            : "<" + Joiner.on(", ").join(FluentIterable.from(type.params())
                .transform(new Function<TypeMetadata, String>() {
                  @Nullable
                  @Override
                  public String apply(@Nullable TypeMetadata input) {
                    return input.toString(imports, true);
                  }
                })) + ">",
        clazz.category() == ClassMetadata.Category.CLASS ? "extends" : "implements",
        type.toString(imports));
  }

  private void writeConstructor(
      Writer writer, final Imports imports, ConstructorMetadata constructor) throws IOException {
    writeLine(writer, "");
    writeLine(writer, "  %sAuto_%s%s_Impl(%s) {",
        constructor.visibility().prefix(),
        constructor.type().nestingPrefix("_"),
        constructor.type().name(),
        Joiner.on(", ").join(FluentIterable.from(constructor.parameters())
            .transform(new Function<ParameterMetadata, String>() {
              @Nullable
              @Override
              public String apply(@Nullable ParameterMetadata input) {
                return input.toString(imports);
              }
            })));
    writeLine(writer, "    super(%s);", Joiner.on(", ").join(FluentIterable.from(constructor.parameters())
        .transform(new Function<ParameterMetadata, String>() {
          @Nullable
          @Override
          public String apply(@Nullable ParameterMetadata input) {
            return input.name();
          }
        })));
    writeLine(writer, "  }");
  }

  private void writeMethod(Writer writer,
      AutoImpl autoImpl,
      Imports imports,
      MethodMetadata method) throws IOException {
    switch (optionForMethod(autoImpl, method)) {
      case THROW_EXCEPTION:
        writeThrowingMethod(writer, imports, method);
        break;
      case RETURN_DEFAULT_VALUE:
        writeDefaultValueMethod(writer, imports, method);
        break;
      default:
        throw new IllegalStateException(
            String.format("Could not determine implementation option for method %s",
                method.toString(imports)));
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

  private void writeThrowingMethod(Writer writer, Imports imports, MethodMetadata method) throws IOException {
    writeLine(writer, "");
    writeLine(writer, "  @Override");
    writeLine(writer, "  %s {", method.toString(imports));
    writeLine(writer,
        "    throw new UnsupportedOperationException(\"The method \\\"%s\\\" is not supported in this implementation.\");",
        method.toString(imports));
    writeLine(writer, "  }");
  }


  private void writeDefaultValueMethod(Writer writer, Imports imports, MethodMetadata method) throws IOException {
    writeLine(writer, "");
    writeLine(writer, "  @Override");
    writeLine(writer, "  %s {", method.toString(imports));
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
