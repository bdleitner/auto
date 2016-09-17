package com.bdl.auto.impl;

import java.util.Set;

/**
 * Interface for classes that report needed imports.
 *
 * @author Ben Leitner
 */
interface GeneratesImports {

  Set<String> getImports();
}
