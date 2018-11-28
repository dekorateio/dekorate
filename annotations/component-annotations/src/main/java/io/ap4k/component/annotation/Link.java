package io.ap4k.component.annotation;

import io.ap4k.kubernetes.annotation.Env;

public @interface Link {
  String name();
  String targetcomponentname();
  String kind();
  String ref();
  Env[] envVars() default {};
}
