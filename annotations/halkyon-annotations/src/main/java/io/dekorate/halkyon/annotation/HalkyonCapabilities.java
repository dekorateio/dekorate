package io.dekorate.halkyon.annotation;

import io.dekorate.kubernetes.config.Configuration;
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Buildable(builderPackage = "io.dekorate.deps.kubernetes.api.builder")
@Pojo(name = "CapabilitiesConfig", mutable = true, superClass = Configuration.class, relativePath = "../config", withStaticAdapterMethod = false,
  adapter = @Adapter(suffix = "Adapter", relativePath = "../adapter", withMapAdapterMethod = true))
@Target({ElementType.CONSTRUCTOR, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface HalkyonCapabilities {

  HalkyonComponentCapability[] provides() default {};

  HalkyonRequiredCapability[] requires() default {};
}
