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
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

@Buildable(builderPackage = "io.ap4k.deps.kubernetes.api.builder")
@Pojo(name = "S2iConfig", mutable = true, superClass = Configuration.class, relativePath = "../config", withStaticAdapterMethod = false, adapter = @Adapter(name = "S2iConfigAdapter", relativePath = "../adapter"))
public @interface EnableS2iBuild {

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


  boolean autoDeployEnabled() default false;

  String builderImage() default "fabric8/s2i-java:2.3";

}
