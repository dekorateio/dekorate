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
 * 
**/

package io.dekorate.s2i.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.kubernetes.annotation.Env;
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

@Buildable(builderPackage = "io.dekorate.deps.kubernetes.api.builder")
@Pojo(name = "S2iBuildConfig", mutable = true, superClass = ImageConfiguration.class, relativePath = "../config", withStaticAdapterMethod = false, adapter = @Adapter(name = "S2iBuildConfigAdapter", relativePath = "../adapter", withMapAdapterMethod = true))
@Target({ ElementType.CONSTRUCTOR, ElementType.TYPE })

@Retention(RetentionPolicy.RUNTIME)
public @interface S2iBuild {

  /**
   * The relative path of the Dockerfile, from the module root.
   * 
   * @return The relative path.
   */
  String dockerFile() default "Dockerfile";

  /**
   * The registry that holds the image.
   * 
   * @return The registry or empty string if no registry has been specified.
   */
  String registry() default "";

  /**
   * The S2i builder image to use.
   * @return The builder image.
   */
  String builderImage() default "fabric8/s2i-java:2.3";

  /**
   * Environment variables to use for the s2i build.
   * @return The environment variables.
   */
  Env[] buildEnvVars() default {};

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
