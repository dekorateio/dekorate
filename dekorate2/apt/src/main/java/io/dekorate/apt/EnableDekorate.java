package io.dekorate.apt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation that triggers Dekorate2 manifest generation at compile time.
 *
 * This annotation is provided for POC / standalone usage. Consuming projects
 * (e.g. Quarkus, Spring Boot integrations) should provide their own annotation
 * processor that bootstraps the Session, rather than relying on this one.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface EnableDekorate {

  /**
   * Configuration file names to read from {@code src/main/resources/}.
   * Defaults to {@code application.properties}.
   */
  String[] resources() default { "application.properties" };
}
