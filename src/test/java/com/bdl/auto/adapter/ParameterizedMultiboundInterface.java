package com.bdl.auto.adapter;

/**
 * Interface with multiple parameters, one of which is multibound.
 *
 * @author Ben Leitner
 */
public interface ParameterizedMultiboundInterface<S, T extends SimpleInterface & ParameterizedInterface<S>> {
}
