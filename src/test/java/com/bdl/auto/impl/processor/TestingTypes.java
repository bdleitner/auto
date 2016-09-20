package com.bdl.auto.impl.processor;

/**
 * Container class for types used in testing.
 *
 * @author Ben Leitner
 */
class TestingTypes {

  static final TypeMetadata AUTO_IMPL = TypeMetadata.builder()
      .setPackageName("com.bdl.auto.impl")
      .setName("AutoImpl")
      .build();

  static final TypeMetadata METHOD_IMPL = TypeMetadata.builder()
      .setPackageName("com.bdl.auto.impl")
      .setName("MethodImpl")
      .build();

  static final TypeMetadata IMPL_OPTION = TypeMetadata.builder()
      .setPackageName("com.bdl.auto.impl")
      .setName("ImplOption")
      .build();

  static final TypeMetadata THING = TypeMetadata.builder()
      .setPackageName("com.bdl.auto.impl.processor")
      .setName("Thing")
      .build();

  static final TypeMetadata PARAM_T = TypeMetadata.simpleTypeParam("T");

  static final TypeMetadata PARAM_T_EXTENDS_FOO = TypeMetadata.builder()
      .setIsTypeParameter(true)
      .setName("T")
      .addBound(TypeMetadata.builder()
          .setName("Foo")
          .build())
      .build();

  static final TypeMetadata PARAM_S = TypeMetadata.simpleTypeParam("S");

  private TestingTypes() {
    // Container class, no instantiation.
  }
}
