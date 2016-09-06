package com.bdl.auto.adapter;

/**
 * Testing interface for more complex generics.
 *
 * @author Ben Leitner
 */
@AutoAdapter
public interface FieldMath<F extends Field<F>> {

  F square(F input);

  F sqrt(F input);
}
