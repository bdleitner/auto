package com.bdl.auto.adapter;

import com.google.common.collect.ImmutableList;

/**
 * Interface for classes that generate methods that need implementing.
 *
 * @author Ben Leitner
 */
public interface GeneratesMethods {

  ImmutableList<MethodMetadata> getOrderedNeededMethods();

}
