package io.dekorate.servicebinding.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.project.BuildInfo;
import io.dekorate.project.Project;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;
import io.sundr.builder.annotations.Pojo;

@Buildable(builderPackage = "io.fabric8.kubernetes.api.builder", refs = {
    @BuildableReference(Project.class),
    @BuildableReference(BuildInfo.class)
})
@Pojo(name = "BindingPathConfig", autobox = true, mutable = true, superClass = Configuration.class, relativePath = "../config", withStaticAdapterMethod = false)
@Target({ ElementType.CONSTRUCTOR, ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
public @interface BindingPath {

  String containerPath() default "";

  String secretPath() default "";

}
