package com.bdl.auto.impl.processor;

import com.google.common.collect.ImmutableList;

/**
 * Interface for classes that generate methods that need implementing.
 *
 * @author Ben Leitner
 */
interface GeneratesMethods {

  ImmutableList<MethodMetadata> getAllMethods();

}
