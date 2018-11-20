package io.ap4k.istio.annotation;

import io.ap4k.config.Configuration;
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Buildable(builderPackage = "io.ap4k.deps.kubernetes.api.builder")
@Pojo(name = "IstioConfig", superClass = Configuration.class, relativePath = "../config", withStaticAdapterMethod = false, adapter = @Adapter(relativePath = "../adapt"))
@Target({ElementType.CONSTRUCTOR, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Istio {

    String version() default "1.0.0";

    ProxyConfig proxyConfig() default @ProxyConfig();
}
