package io.ap4k.docker.annotation;

import io.ap4k.annotation.Internal;
import io.ap4k.config.Configuration;
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Internal
@Buildable(builderPackage = "io.ap4k.deps.kubernetes.api.builder")
@Pojo(suffix = "Config", relativePath = "../config",
  superClass = Configuration.class,
  withStaticBuilderMethod = false,
  withStaticAdapterMethod = false,
  adapter = @Adapter(suffix = "Adapter", relativePath = "../adapter"))
@Target({ElementType.CONSTRUCTOR, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface DockerBuild {

  /**
   * The group of the application.
   * This value will be use as:
   * - docker image repo
   * - labeling resources
   * @return The specified group name.
   */
  String group() default "";

  /**
   * The name of the application.
   * This value will be used for naming Kubernetes resources like:
   * - Deployment
   * - Service
   * and so on ...
   * If no value is specified it will attempt to determine the name using the following rules:
   * If its a maven/gradle project use the artifact id.
   * Else if its a bazel project use the name.
   * Else if the system property app.name is present it will be used.
   * Else find the project root folder and use its name (root folder detection is done by moving to the parent folder until .git is found).
   * @return The specified application name.
   */
  String name() default "";

  /**
   * The version of the application.
   * This value be used for things like:
   * - The docker image tag.
   * If no value specified it will attempt to determine the name using the following rules:
   * @return The version.
   */
  String version() default "";

  /**
   * The relative path of the Dockerfile, from the module root.
   * @return  The relative path.
   */
  String dockerFile() default  "Dockerfile";


  /**
   * Flag to automatically register a build hook after compilation.
   * @return  True if hook is to be registered, false otherwise.
   */
  boolean isAutoBuildEnabled() default false;
}
