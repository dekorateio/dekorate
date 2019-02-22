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


package io.ap4k.openshift.annotation;

import io.ap4k.kubernetes.config.Configuration;
import io.ap4k.kubernetes.annotation.Env;
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Buildable(builderPackage = "io.ap4k.deps.kubernetes.api.builder")
@Pojo(name = "S2iConfig", mutable = true, superClass = Configuration.class, relativePath = "../config", withStaticAdapterMethod = false, adapter = @Adapter(name = "S2iConfigAdapter", relativePath = "../adapter", withMapAdapterMethod = true))
@Target({ElementType.CONSTRUCTOR, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableS2iBuild {

  /**
   * The S2i builder image to use.
   * @return The builder image.
   */
  String builderImage() default "fabric8/s2i-java:2.3";

  /**
   * Environment variables to add to all containers.
   * @return The environment variables.
   */
  Env[] envVars() default {};

  /**
   * Flag to trigger the registration of the build hook.
   * It's generally preferable to use `-Dap4k.build=true` instead of hardcoding this here.
   * @return  True for automatic registration of the build hook.
   */
  boolean autoBuildEnabled() default false;


  /**
   * Flag to trigger the registration of the deploy hook.
   * It's generally preferable to use `-Dap4k.deploy=true` instead of hardcoding this here.
   * @return  True for automatic registration of the build hook.
   */
  boolean autoDeployEnabled() default false;
}
