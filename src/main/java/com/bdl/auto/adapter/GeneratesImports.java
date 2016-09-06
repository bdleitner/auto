package com.bdl.auto.adapter;

import com.google.common.collect.ImmutableList;

/**
 * Interface for classes that report needed imports.
 *
 * @author Ben Leitner
 */
interface GeneratesImports {

  ImmutableList<String> getImports();
}
