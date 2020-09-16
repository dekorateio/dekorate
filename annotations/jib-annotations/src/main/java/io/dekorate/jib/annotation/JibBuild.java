/**
 * Copyright 2018 The original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.jib.annotation;

import io.dekorate.kubernetes.config.ImageConfiguration;
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Buildable(builderPackage = "io.dekorate.deps.kubernetes.api.builder")

@Pojo(name = "JibBuildConfig", mutable = true, superClass = ImageConfiguration.class, relativePath = "../config", withStaticAdapterMethod = false, adapter = @Adapter(name = "JibBuildConfigAdapter", relativePath = "../adapter", withMapAdapterMethod = true))
@Target({ElementType.CONSTRUCTOR, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface JibBuild {

  boolean enabled() default true;
  /**
   * The registry that holds the image.
   * 
   * @return The registry or empty string if no registry has been specified.
   */
  String registry() default "";

  /**
   * The group of the application. This value will be use as image user. 
   * @return The specified group name.
   */
  String group() default "";

  /**
   * The name of the application. This value will be used as name.
   * @return The specified application name.
   */
  String name() default "";

  /**
   * The version of the application. This value be used as image tag.
   * @return The version.
   */
  String version() default "";

  /**
   * The name of the image to be generated.
   * This property overrides group, name and version.
   * @return the image name.
   */
  String image() default "";

  /**
   * Flag that indicates whether to perform a docker build (build using the docker daemon) or not.
   * @return true, if docker build is desired, false otherwise.
   */
  boolean dockerBuild() default true;

  /**
   * The base image to use.
   * @return The base image.
   */
  String from() default  "openjdk:8-jdk";


  /**
   * Flag to automatically push the image, to the specified registry.
   * 
   * @return True if hook is to be registered, false otherwise.
   */
  boolean autoPushEnabled() default false;

  /**
   * Flag to automatically register a build hook after compilation.
   * 
   * @return True if hook is to be registered, false otherwise.
   */
  boolean autoBuildEnabled() default false;

  /**
   * Flag to trigger the registration of the deploy hook. It's generally
   * preferable to use `-Ddekorate.deploy=true` instead of hardcoding this here.
   * 
   * @return True for automatic registration of the build hook.
   */
  boolean autoDeployEnabled() default false;
}
