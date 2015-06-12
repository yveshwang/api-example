package com.macyves.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Ruleset for merging for each field.
 * 
 * @author yves
 * 
 */
@Target({ FIELD })
@Retention(RUNTIME)
public @interface Merge {
    boolean null_allowed() default true;

    boolean no_merge() default false;
}
