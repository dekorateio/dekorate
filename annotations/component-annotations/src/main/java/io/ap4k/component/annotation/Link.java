package io.ap4k.component.annotation;

import io.ap4k.component.model.Kind;
import io.ap4k.kubernetes.annotation.Env;
import io.ap4k.kubernetes.config.Configuration;
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Buildable(builderPackage = "io.ap4k.deps.kubernetes.api.builder")
@Pojo(name = "LinkConfig", mutable = true, superClass = Configuration.class, relativePath = "../config", withStaticAdapterMethod = false,
  adapter = @Adapter(suffix = "Adapter", relativePath = "../adapter", withMapAdapterMethod = true))
@Target({ElementType.CONSTRUCTOR, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Link {
  String name() default "";;
  String componentName();
  Kind kind() default Kind.Env;
  String ref() default "";;
  Env[] envVars() default {};
}
