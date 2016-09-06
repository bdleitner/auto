package com.bdl.auto.adapter;

/**
 * @author Ben Leitner
 */
public interface Field<F extends Field<F>> {

  F add(F that);
  F subtract(F that);
  F multiply(F that);
  F divide(F that);
}
