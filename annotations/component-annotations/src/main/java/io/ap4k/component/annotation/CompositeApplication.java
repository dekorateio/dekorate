package io.ap4k.component.annotation;


import io.ap4k.component.model.DeploymentType;
import io.ap4k.config.KubernetesConfig;
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Buildable(builderPackage = "io.ap4k.deps.kubernetes.api.builder")
@Pojo(name = "CompositeConfig", superClass = KubernetesConfig.class, relativePath = "../config", withStaticAdapterMethod = false, adapter = @Adapter(relativePath = "../adapt"))
@Target({ElementType.CONSTRUCTOR, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface CompositeApplication {

  String name() default "";

  DeploymentType deploymentType() default DeploymentType.innerloop;

  boolean exposeService() default false;

  /*

  Env[] envVars() default {};

  ServiceCatalogInstance[] instances();

  */
}
