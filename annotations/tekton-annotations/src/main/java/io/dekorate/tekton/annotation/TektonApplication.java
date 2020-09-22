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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.dekorate.kubernetes.annotation.Annotation;
import io.dekorate.kubernetes.annotation.Label;
import io.dekorate.kubernetes.annotation.PersistentVolumeClaim;
import io.dekorate.kubernetes.config.ApplicationConfiguration;
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

@Buildable(builderPackage = "io.fabric8.kubernetes.api.builder")
@Pojo(name = "TektonConfig", mutable = true, superClass = ApplicationConfiguration.class, relativePath = "../config", withStaticAdapterMethod = false, adapter = @Adapter(relativePath = "../adapter", withMapAdapterMethod = true))
@Target({ ElementType.CONSTRUCTOR, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface TektonApplication {

  String DEFAULT_DEPLOYER_IMAGE = "lachlanevenson/k8s-kubectl:v1.18.0";

  /**
   * The name of the collection of componnet this component belongs to.
   * This value will be use as:
   * - labeling resources
   * 
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
   * Else find the project root folder and use its name (root folder detection is done by moving to the parent folder until
   * .git is found).
   * 
   * @return The specified application name.
   */
  String name() default "";

  /**
   * The version of the application.
   * This value be used for things like:
   * - The docker image tag.
   * If no value specified it will attempt to determine the name using the following rules:
   * 
   * @return The version.
   */
  String version() default "";

  /**
   * Custom labels to add to all resources.
   * 
   * @return The labels.
   */
  Label[] labels() default {};

  /**
   * Custom annotations to add to all resources.
   * 
   * @return The annotations.
   */
  Annotation[] annotations() default {};

  /*
   * The name of an external git pipeline resource.
   * 
   * @return The name of the resource, or empty if none is specified.
   */
  String externalGitPipelineResource() default "";

  /*
   * The name of the source workspace.
   * 
   * @return the name, or 'source' if no name specified.
   */
  String sourceWorkspace() default "source";

  /**
   * The name of an external PVC to be used for the source workspace.
   * 
   * @return the name or empty String if the PVC is meant to be generated.
   */
  String externalSourceWorkspaceClaim() default "";

  /*
   * The persistent volume claim configuration for the source workspace.
   * The option only makes sense when the PVC is going to be generated (no external pvc specified).
   * 
   * @return The PVC configuration.
   */
  PersistentVolumeClaim sourceWorkspaceClaim() default @PersistentVolumeClaim();

  /**
   * The name of workspace to use as a maven artifact repository.
   * 
   * @return the workspace name.
   */
  String m2Workspace() default "m2";

  /**
   * The name of an external PVC to be used for the m2 artifact repository.
   * 
   * @return the name or empty String if the PVC is meant to be generated.
   */
  String externalM2WorkspaceClaim() default "";

  /*
   * The persistent volume claim configuration for the artifact repository.
   * The option only makes sense when the PVC is going to be generated (no external pvc specified).
   * 
   * @return The PVC configuration.
   */
  PersistentVolumeClaim m2WorkspaceClaim() default @PersistentVolumeClaim();

  /**
   * The builder image to use.
   * 
   * @return The builder image, or empty if the image should be inferred.
   */
  String builderImage() default "";

  /*
   * The builder command to use.
   * 
   * @return The builder command or empty if the command is to be inferred
   */
  String builderCommand() default "";

  /*
   * The builder command arguments to use.
   * 
   * @return The builder command arguments or empty if the command is to be inferred
   */
  String[] builderArguments() default {};

  /**
   * The docker image to be used for the deployment task.
   * Such image needs to have kubectl available.
   * 
   * @return The image, if specified or fallback to the default otherwise.
   */
  String deployerImage() default DEFAULT_DEPLOYER_IMAGE;

  /*
   * The service account to use for the image pushing tasks.
   * An existing or a generated service account can be used.
   * If no existing service account is provided one will be generated based on the context.
   * 
   * @return An existing service account, or empty if we just need to generate one.
   */
  String imagePushServiceAccount() default "";

  /**
   * The secret to use when generating an image push service account.
   * When no existing service account is provided, one will be generated.
   * The generated service account may or may not use an existing secret.
   * 
   * @return The existing secret, or empty string if the secret needs to be generated.
   */
  String imagePushSecret() default "";

  /*
   * Wether to upload the local `.docker/config.json` to automatically create the secret.
   * 
   * @return
   */
  boolean useLocalDockerConfigJson() default false;

  /*
   * The username to use for generating image builder secrets.
   * 
   * @return The username.
   */
  String registry() default "docker.io";

  /*
   * The username to use for generating image builder secrets.
   * 
   * @return The username.
   */
  String registryUsername() default "";

  /*
   * The password to use for generating image builder secrets.
   * 
   * @return The password.
   */
  String registryPassword() default "";

}
