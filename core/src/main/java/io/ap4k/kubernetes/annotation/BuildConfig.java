package io.ap4k.kubernetes.annotation;

public @interface BuildConfig {
  String type() default "s2i";
}
