package io.ap4k.testing.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A name qualifier annotation.
 */

@Target({ FIELD })
@Retention(RUNTIME)
public @interface Named {

  /**
   * The name of the annotated resource.
   * This annotation is used as a qualifier for injection.
   * @return
   */
  String value();
}
