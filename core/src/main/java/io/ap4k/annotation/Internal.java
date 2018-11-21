package io.ap4k.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks classes that are meant for internal consumption.
 * A typical example is classes that are used for own testing.
 */
@Target({ElementType.CONSTRUCTOR, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Internal {
}
