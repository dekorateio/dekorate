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
package io.dekorate.knative.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.dekorate.knative.config.AutoScalerClass;
import io.dekorate.knative.config.AutoscalingMetric;
import io.dekorate.knative.config.HttpTransportVersion;
import io.dekorate.kubernetes.annotation.Annotation;
import io.dekorate.kubernetes.annotation.AwsElasticBlockStoreVolume;
import io.dekorate.kubernetes.annotation.AzureDiskVolume;
import io.dekorate.kubernetes.annotation.AzureFileVolume;
import io.dekorate.kubernetes.annotation.ConfigMapVolume;
import io.dekorate.kubernetes.annotation.Container;
import io.dekorate.kubernetes.annotation.CronJob;
import io.dekorate.kubernetes.annotation.EmptyDirVolume;
import io.dekorate.kubernetes.annotation.Env;
import io.dekorate.kubernetes.annotation.GitRepoVolume;
import io.dekorate.kubernetes.annotation.ImagePullPolicy;
import io.dekorate.kubernetes.annotation.Job;
import io.dekorate.kubernetes.annotation.Label;
import io.dekorate.kubernetes.annotation.Mount;
import io.dekorate.kubernetes.annotation.PersistentVolumeClaimVolume;
import io.dekorate.kubernetes.annotation.Port;
import io.dekorate.kubernetes.annotation.Probe;
import io.dekorate.kubernetes.annotation.ResourceRequirements;
import io.dekorate.kubernetes.annotation.SecretVolume;
import io.dekorate.kubernetes.annotation.ServiceType;
import io.dekorate.kubernetes.config.BaseConfig;
import io.dekorate.kubernetes.config.HostAlias;
import io.dekorate.kubernetes.config.NodeSelector;
import io.dekorate.kubernetes.config.RollingUpdate;
import io.dekorate.project.BuildInfo;
import io.dekorate.project.Project;
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;
import io.sundr.builder.annotations.Pojo;

@Buildable(builderPackage = "io.fabric8.kubernetes.api.builder", refs = {
    @BuildableReference(Project.class),
    @BuildableReference(BuildInfo.class),
    @BuildableReference(HostAlias.class),
    @BuildableReference(RollingUpdate.class),
    @BuildableReference(NodeSelector.class)
})
@Pojo(name = "KnativeConfig", autobox = true, mutable = true, superClass = BaseConfig.class, relativePath = "../config", withStaticAdapterMethod = false, adapter = @Adapter(relativePath = "../adapter", withMapAdapterMethod = true))
@Target({ ElementType.CONSTRUCTOR, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface KnativeApplication {

  int DEFAULT_CONTAINER_CONCURRENCY = 0;
  AutoscalingMetric DEFAULT_AUTOSCALING_METRIC = AutoscalingMetric.concurrency;

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
   *
   */
  String revisionName() default "";

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
   * Http trasport version to use.
   */
  HttpTransportVersion httpTransportVersion() default HttpTransportVersion.HTTP1;

  /**
   * The type of service that will be generated for the application.
   */
  ServiceType serviceType() default ServiceType.ClusterIP;

  /**
   * PersistentVolumeClaim volumues to add to all containers.
   */
  PersistentVolumeClaimVolume[] pvcVolumes() default {};

  /**
   * Secret volumues to add to all containers.
   */
  SecretVolume[] secretVolumes() default {};

  /**
   * ConfigMap volumues to add to all containers.
   */
  ConfigMapVolume[] configMapVolumes() default {};

  /**
   * The EmptyDir volumes to add to all containers.
   */
  EmptyDirVolume[] emptyDirVolumes() default {};

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
   * The startup probe.
   *
   * @return The probe.
   */
  Probe startupProbe() default @Probe();

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
   * Controls whether the application should be exposed (default: true).
   * Services that are not exposed with be labeled as cluster local (see
   * https://knative.dev/docs/serving/services/private-services).
   */
  boolean expose() default true;

  /**
   * Flag to trigger the registration of the deploy hook.
   * It's generally preferable to use `-Ddekorate.deploy=true` instead of hardcoding this here.
   *
   * @return True for automatic registration of the build hook.
   */
  boolean autoDeployEnabled() default false;

  /**
   * This value controls the minimum number of replicas each revision should have.
   * Knative will attempt to never have less than this number of replicas at any one point in time.
   */
  int minScale() default 0;

  /**
   * This value controls the maximum number of replicas each revision should have.
   * Knative will attempt to never have more than this number of replicas running, or in the process of being created, at any
   * one point in time.
   **/
  int maxScale() default 0;

  /**
   * The scale-to-zero values control whether Knative allows revisions to scale down to zero, or stops at “1”.
   */
  boolean scaleToZeroEnabled() default true;

  /**
   * Revision autoscaling configuration.
   */
  AutoScaling revisionAutoScaling() default @AutoScaling(autoScalerClass = AutoScalerClass.kpa, metric = AutoscalingMetric.concurrency);

  /**
   * Global autoscaling configuration.
   */
  GlobalAutoScaling globalAutoScaling() default @GlobalAutoScaling(autoScalerClass = AutoScalerClass.kpa);

  /**
   * The template name to specify.
   */
  String templateName() default "";

  /**
   * Traffic configuration for the application.
   */
  Traffic[] traffic() default {};

  /**
   * The Kubernetes Jobs.
   *
   * @return the jobs;
   */
  Job[] jobs() default {};

  /**
   * The Kubernetes CronJobs.
   *
   * @return the cronJobs;
   */
  CronJob[] cronJobs() default {};

}
