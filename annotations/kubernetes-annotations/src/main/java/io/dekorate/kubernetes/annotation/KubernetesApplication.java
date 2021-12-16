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
package io.dekorate.kubernetes.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.dekorate.kubernetes.config.BaseConfig;
import io.dekorate.kubernetes.config.DeploymentStrategy;
import io.fabric8.kubernetes.api.model.Service;
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

@Buildable(builderPackage = "io.fabric8.kubernetes.api.builder")
@Pojo(name = "KubernetesConfig", relativePath = "../config", autobox = true, mutable = true, superClass = BaseConfig.class, withStaticBuilderMethod = true, withStaticAdapterMethod = false, adapter = @Adapter(suffix = "Adapter", relativePath = "../adapter", withMapAdapterMethod = true))
@Target({ ElementType.CONSTRUCTOR, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface KubernetesApplication {

  /**
   * The name of the collection of component this component belongs to.
   * This value will be use as:
   * - docker image repo
   * - labeling resources
   * 
   * @return The specified group name.
   */
  String partOf() default "";

  /**
   * The name of the application. This value will be used for naming Kubernetes
   * resources like: - Deployment - Service and so on ... If no value is specified
   * it will attempt to determine the name using the following rules: If its a
   * maven/gradle project use the artifact id. Else if its a bazel project use the
   * name. Else if the system property app.name is present it will be used. Else
   * find the project root folder and use its name (root folder detection is done
   * by moving to the parent folder until .git is found).
   * 
   * @return The specified application name.
   */
  String name() default "";

  /**
   * The version of the application. This value be used for things like: - The
   * docker image tag. If no value specified it will attempt to determine the name
   * using the following rules:
   * 
   * @return The version.
   */
  String version() default "";

  /**
   * The init containers.
   * 
   * @return the init containers.
   */
  Container[] initContainers() default {};

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

  /**
   * Environment variables to add to all containers.
   * 
   * @return The environment variables.
   */
  Env[] envVars() default {};

  /**
   * Working directory.
   * 
   * @return The working directory if specified, else empty string.
   */
  String workingDir() default "";

  /**
   * The commands
   * 
   * @return The commands.
   */
  String[] command() default {};

  /**
   * The arguments
   * 
   * @return The arguments.
   */
  String[] arguments() default {};

  /**
   * The number of replicas to use.
   * 
   * @return The number of replicas.
   */
  int replicas() default 1;

  /**
   * Specifies the deployment strategy.
   */
  DeploymentStrategy deploymentStrategy() default DeploymentStrategy.None;

  /**
   * Specifies rolling update configuration.
   * The configuration is applied when DeploymentStrategy == Rolling update, or
   * when explicit configuration has been provided. In the later case RollingUpdate is assumed.
   */
  RollingUpdate rollingUpdate() default @RollingUpdate;

  /**
   * The service account.
   * 
   * @return The service account or empty string if not specified.
   */
  String serviceAccount() default "";

  /**
   * The host under which the application is going to be exposed.
   * 
   * @return The hostname.
   */
  String host() default "";

  /**
   * The application ports.
   */
  Port[] ports() default {};

  /**
   * The type of service that will be generated for the application.
   */
  ServiceType serviceType() default ServiceType.ClusterIP;

  /**
   * PersistentVolumeClaim volumes to add to all containers.
   */
  PersistentVolumeClaimVolume[] pvcVolumes() default {};

  /**
   * Secret volumes to add to all containers.
   */
  SecretVolume[] secretVolumes() default {};

  /**
   * ConfigMap volumues to add to all containers.
   */
  ConfigMapVolume[] configMapVolumes() default {};

  /**
   * Git repo volumues to add to all containers.
   */
  GitRepoVolume[] gitRepoVolumes() default {};

  /**
   * Aws elastic block store volumes to add to all containers
   */
  AwsElasticBlockStoreVolume[] awsElasticBlockStoreVolumes() default {};

  /**
   * Azure disk volumes to add
   */
  AzureDiskVolume[] azureDiskVolumes() default {};

  /**
   * Azure file volumes to add
   */
  AzureFileVolume[] azureFileVolumes() default {};

  /**
   * Mounts to add to all containers.
   * 
   * @return The mounts.
   */
  Mount[] mounts() default {};

  /**
   * Image pull policy.
   * 
   * @return The image pull policy.
   */
  ImagePullPolicy imagePullPolicy() default ImagePullPolicy.IfNotPresent;

  /**
   * The image pull secret
   */
  String[] imagePullSecrets() default {};

  /**
   * The hostAliases
   */
  HostAlias[] hostAliases() default {};

  /**
   * The liveness probe.
   * 
   * @return The probe.
   */
  Probe livenessProbe() default @Probe();

  /**
   * The readiness probe.
   * 
   * @return The probe.
   */
  Probe readinessProbe() default @Probe();

  /**
   * The resources that the application container requires.
   */
  ResourceRequirements requestResources() default @ResourceRequirements();

  /**
   * The resource limit for the application container.
   */
  ResourceRequirements limitResources() default @ResourceRequirements();

  /**
   * The sidecars.
   * 
   * @return the sidecar containers.
   */
  Container[] sidecars() default {};

  /**
   * Controls whether the application should be exposed via Ingress
   */
  boolean expose() default false;

  /**
   * Controls whether the generated {@link Service} will be headless.
   * 
   * @return true if headless.
   */
  boolean headless() default false;

  /**
   * Flag to trigger the registration of the deploy hook. It's generally
   * preferable to use `-Ddekorate.deploy=true` instead of hardcoding this here.
   * 
   * @return True for automatic registration of the build hook.
   */
  boolean autoDeployEnabled() default false;
}
