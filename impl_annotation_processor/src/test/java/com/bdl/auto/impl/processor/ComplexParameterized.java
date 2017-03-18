package com.bdl.auto.impl.processor;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Interface for testing complex parameterization.
 *
 * @author Ben Leitner
 */
interface ComplexParameterized<X, Y extends Comparable<Y>, Z extends List<Y>> {

  <A, B extends List<A>> ImmutableList<A> filter(B source, Predicate<A> predicate);

  <T> T extend(Z list, T template);
}
