package com.bdl.auto.impl.processor;

import com.bdl.auto.impl.AutoImpl;

/**
 * Nested classes for testing.
 *
 * @author Ben Leitner
 */
@SuppressWarnings("unused") // Used via compiler element search in TypeMetadataTest.
class TopLevel {

  static class Outer {

    @AutoImpl
    interface Inner {

    }
  }
}
