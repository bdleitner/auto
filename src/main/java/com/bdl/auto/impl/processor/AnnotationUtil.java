package com.bdl.auto.impl.processor;

import com.google.auto.value.AutoAnnotation;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import com.bdl.annotation.processing.model.AnnotationMetadata;
import com.bdl.annotation.processing.model.ClassMetadata;
import com.bdl.annotation.processing.model.MethodMetadata;
import com.bdl.annotation.processing.model.ValueMetadata;
import com.bdl.auto.impl.AutoImpl;
import com.bdl.auto.impl.ImplOption;
import com.bdl.auto.impl.MethodImpl;

import java.util.Map;

/**
 * Utility class for working with the annotations.
 *
 * @author Ben Leitner
 */
class AnnotationUtil {
  private AnnotationUtil() {}

  @AutoAnnotation
  static AutoImpl autoImpl(
      ImplOption value,
      ImplOption numericImpl,
      ImplOption booleanImpl,
      ImplOption voidImpl,
      ImplOption stringImpl,
      ImplOption objectImpl) {
    return new AutoAnnotation_AnnotationUtil_autoImpl(
        value,
        numericImpl,
        booleanImpl,
        voidImpl,
        stringImpl,
        objectImpl);
  }

  static AutoImpl autoImpl(AnnotationMetadata metadata) {
    Preconditions.checkArgument(metadata.type().packageName().equals("com.bdl.auto.impl")
        && metadata.type().name().equals("AutoImpl"));
    Map<String, ImplOption> map = Maps.newHashMap();
    map.put("value", ImplOption.THROW_EXCEPTION);
    map.put("numericImpl", ImplOption.USE_PARENT);
    map.put("booleanImpl", ImplOption.USE_PARENT);
    map.put("voidImpl", ImplOption.USE_PARENT);
    map.put("stringImpl", ImplOption.USE_PARENT);
    map.put("objectImpl", ImplOption.USE_PARENT);

    for (Map.Entry<MethodMetadata, ValueMetadata> entry : metadata.values().entrySet()) {
      map.put(
          entry.getKey().name(),
          ImplOption.valueOf(entry.getValue().value()));
    }
    return autoImpl(
        map.get("value"),
        map.get("numericImpl"),
        map.get("booleanImpl"),
        map.get("voidImpl"),
        map.get("stringImpl"),
        map.get("objectImpl"));

  }

  static AutoImpl autoImpl(ClassMetadata metadata) {
    for (AnnotationMetadata annotation : metadata.annotations()) {
      if (annotation.type().packageName().equals("com.bdl.auto.impl")
          && annotation.type().name().equals("AutoImpl")) {
        return autoImpl(annotation);
      }
    }
    return autoImpl(
        ImplOption.THROW_EXCEPTION,
        ImplOption.USE_PARENT,
        ImplOption.USE_PARENT,
        ImplOption.USE_PARENT,
        ImplOption.USE_PARENT,
        ImplOption.USE_PARENT);
  }

  @AutoAnnotation
  static MethodImpl methodImpl(ImplOption value) {
    return new AutoAnnotation_AnnotationUtil_methodImpl(value);
  }

  static MethodImpl methodImpl(MethodMetadata method) {
    for (AnnotationMetadata annotation : method.annotations()) {
      if (annotation.type().packageName().equals("com.bdl.auto.impl")
        && annotation.type().name().equals("MethodImpl")) {
        for (Map.Entry<MethodMetadata, ValueMetadata> entry : annotation.values().entrySet()) {
          if (entry.getKey().name().equals("value")) {
            return methodImpl(ImplOption.valueOf(entry.getValue().value()));
          }
        }
      }
    }
    return methodImpl(ImplOption.USE_PARENT);
  }
}
