package com.bdl.auto.adapter;

/**
 * Utility class for working with string type names.
 *
 * @author Ben Leitner
 */
class TypeUtil {
  private static final String JAVA_LANG_PREFIX = "java.lang.";

  private TypeUtil() {
    // Utility class, No instantiation.
  }

  public static String normalize(String typeName) {
    if (typeName.startsWith(JAVA_LANG_PREFIX)) {
      return typeName.substring(JAVA_LANG_PREFIX.length());
    }
    return typeName;
  }
}
