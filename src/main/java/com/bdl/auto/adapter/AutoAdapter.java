package com.bdl.auto.adapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class should have concrete AutoAdapter classes generated for it.
 *
 * @author Ben Leitner
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface AutoAdapter {
}
