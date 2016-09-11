package com.bdl.auto.adapter;

/**
 * Container class for types used in testing.
 *
 * @author Ben Leitner
 */
class TestingTypes {

  static final TypeMetadata THING = TypeMetadata.builder()
      .setPackageName("com.bdl.auto.adapter")
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
