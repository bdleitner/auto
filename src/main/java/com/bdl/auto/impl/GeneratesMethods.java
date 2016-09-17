package com.bdl.auto.impl;

import com.google.common.collect.ImmutableList;

/**
 * Interface for classes that generate methods that need implementing.
 *
 * @author Ben Leitner
 */
public interface GeneratesMethods {

  ImmutableList<MethodMetadata> getAllMethods();

}
