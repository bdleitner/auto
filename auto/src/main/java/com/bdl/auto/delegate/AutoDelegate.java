package com.bdl.auto.delegate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a class as needing a generated delegate for all unimplemented methods.
 *
 * TODO: Make this annotation have SOURCE-retention.  Combine the annnotations and processor
 * into one dependency that can be compileOnly/apt
 *
 * @author Ben Leitner
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface AutoDelegate {
}
