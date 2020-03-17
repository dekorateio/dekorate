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
package io.dekorate.tekton.annotation;

import io.dekorate.kubernetes.annotation.Annotation;
import io.dekorate.kubernetes.annotation.AwsElasticBlockStoreVolume;
import io.dekorate.kubernetes.annotation.AzureDiskVolume;
import io.dekorate.kubernetes.annotation.AzureFileVolume;
import io.dekorate.kubernetes.annotation.Container;
import io.dekorate.kubernetes.annotation.GitRepoVolume;
import io.dekorate.kubernetes.annotation.ImagePullPolicy;
import io.dekorate.kubernetes.annotation.Label;
import io.dekorate.kubernetes.annotation.Mount;
import io.dekorate.kubernetes.annotation.PersistentVolumeClaimVolume;
import io.dekorate.kubernetes.annotation.Port;
import io.dekorate.kubernetes.annotation.Probe;
import io.dekorate.kubernetes.annotation.SecretVolume;
import io.dekorate.kubernetes.annotation.ServiceType;
import io.dekorate.kubernetes.annotation.ConfigMapVolume;
import io.dekorate.kubernetes.annotation.Env;
import io.dekorate.kubernetes.config.ApplicationConfiguration;
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Buildable(builderPackage = "io.dekorate.deps.kubernetes.api.builder")
@Pojo(name = "TektonConfig", mutable = true, superClass = ApplicationConfiguration.class, relativePath = "../config", withStaticAdapterMethod = false, adapter = @Adapter(relativePath = "../adapter", withMapAdapterMethod = true))
@Target({ElementType.CONSTRUCTOR, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TektonApplication {

  String DEFAULT_ARTIFACT_REPOSITORY_PATH = "/workspaces/maven";
  String DEFAULT_DEPLOYER_IMAGE = "lachlanevenson/k8s-kubectl:v1.18.0";

  /**
   * The name of the collection of componnet this component belongs to.
   * This value will be use as:
   * - labeling resources
   * @return The specified group name.
   */
  String partOf() default "";

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
   * Custom labels to add to all resources.
   * @return The labels.
   */
  Label[] labels() default {};

  /**
   * Custom annotations to add to all resources.
   * @return The annotations.
   */
  Annotation[] annotations() default {};


  /**
   * The name of workspace to use as a maven artifact repository.
   * @return the workspace name.
   */
  String artifactRepositoryWorkspace() default "";

  /**
   * The path where the artifact repository workspace will be mounted.
   * @return the mounting path.
   */
  String artifactRepositoryPath() default DEFAULT_ARTIFACT_REPOSITORY_PATH;


  /**
   * The docker image to be used for the deployment task.
   * Such image needs to have kubectl available.
   * @return The image, if specified or fallback to the default otherwise.
   */
  String deployerImage() default DEFAULT_DEPLOYER_IMAGE;
}
