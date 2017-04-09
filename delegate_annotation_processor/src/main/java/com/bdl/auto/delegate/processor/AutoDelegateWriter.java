package com.bdl.auto.delegate.processor;

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
public class AutoDelegateWriter {

  public interface Recorder {
    void record(String s);
  }

  private final Function<String, Writer> writerFunction;
  private final Recorder log;

  public AutoDelegateWriter(Function<String, Writer> writerFunction, Recorder log) {
    this.writerFunction = writerFunction;
    this.log = log;
  }

  public void write(ClassMetadata clazz) throws IOException {
    TypeMetadata type = clazz.type();
    log.record(String.format("Writing Delegate class for %s", type.fullyQualifiedPathName()));

    Writer writer =
        writerFunction.apply(
            type.packagePrefix() + "Auto_" + type.nestingPrefix("_") + type.name() + "_Delegate");

    ImmutableSet.Builder<TypeMetadata> types = ImmutableSet.builder();
    types.add(clazz.type());
    types.add(TypeMetadata.from(Generated.class));
    List<MethodMetadata> methods = FluentIterable.from(clazz.getAllMethods())
        .filter(new Predicate<MethodMetadata>() {
          @Override
          public boolean apply(@Nullable MethodMetadata input) {
            return input.modifiers().isAbstract();
          }
        })
        .toSortedList(Ordering.<MethodMetadata>natural());
    for (MethodMetadata method : methods) {
      types.addAll(method.getAllTypes());
    }

    final List<ConstructorMetadata> constructors = FluentIterable.from(clazz.constructors())
        .filter(new Predicate<ConstructorMetadata>() {
          @Override
          public boolean apply(@Nullable ConstructorMetadata input) {
            return input.visibility() != Visibility.PRIVATE;
          }
        })
        .toSortedList(Ordering.<ConstructorMetadata>natural());

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

  private void writeClassOpening(Writer writer, ClassMetadata clazz, final Imports imports)
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
                + Joiner.on(", ").join(FluentIterable.from(type.params())
            .transform(new Function<TypeMetadata, String>() {
              @Nullable
              @Override
              public String apply(@Nullable TypeMetadata input) {
                return input.toString(imports, true);
              }
            }))
                + ">",
        type.toString(imports));
  }

  private void writeConstructor(Writer writer, final Imports imports, ConstructorMetadata constructor)
      throws IOException {
    writeLine(writer, "");
    writeLine(
        writer,
        "  %sAuto_%s%s_Delegate(%s) {",
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
    writeLine(
        writer,
        "    super(%s);",
        Joiner.on(", ").join(FluentIterable.from(constructor.parameters())
            .transform(new Function<ParameterMetadata, String>() {
              @Nullable
              @Override
              public String apply(@Nullable ParameterMetadata input) {
                return input.name();
              }
            })));
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
        Joiner.on(", ").join(FluentIterable.from(method.parameters()).transform(new Function<ParameterMetadata, String>() {
          @Nullable
          @Override
          public String apply(@Nullable ParameterMetadata input) {
            return input.name();
          }
        })));
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
