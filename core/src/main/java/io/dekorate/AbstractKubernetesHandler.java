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
package io.dekorate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.dekorate.kubernetes.annotation.ImagePullPolicy;
import io.dekorate.kubernetes.config.Annotation;
import io.dekorate.kubernetes.config.AwsElasticBlockStoreVolume;
import io.dekorate.kubernetes.config.AzureDiskVolume;
import io.dekorate.kubernetes.config.AzureFileVolume;
import io.dekorate.kubernetes.config.ConfigMapVolume;
import io.dekorate.kubernetes.config.Container;
import io.dekorate.kubernetes.config.Env;
import io.dekorate.kubernetes.config.BaseConfig;
import io.dekorate.kubernetes.config.HostAlias;
import io.dekorate.kubernetes.config.Label;
import io.dekorate.kubernetes.config.Mount;
import io.dekorate.kubernetes.config.PersistentVolumeClaimVolume;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.kubernetes.config.SecretVolume;
import io.dekorate.kubernetes.decorator.AddAnnotationDecorator;
import io.dekorate.kubernetes.decorator.AddAwsElasticBlockStoreVolumeDecorator;
import io.dekorate.kubernetes.decorator.AddAzureDiskVolumeDecorator;
import io.dekorate.kubernetes.decorator.AddAzureFileVolumeDecorator;
import io.dekorate.kubernetes.decorator.AddCommitIdAnnotationDecorator;
import io.dekorate.kubernetes.decorator.AddConfigMapVolumeDecorator;
import io.dekorate.kubernetes.decorator.AddEnvVarDecorator;
import io.dekorate.kubernetes.decorator.AddHostAliasesDecorator;
import io.dekorate.kubernetes.decorator.AddImagePullSecretDecorator;
import io.dekorate.kubernetes.decorator.AddLabelDecorator;
import io.dekorate.kubernetes.decorator.AddLivenessProbeDecorator;
import io.dekorate.kubernetes.decorator.AddMountDecorator;
import io.dekorate.kubernetes.decorator.AddPortDecorator;
import io.dekorate.kubernetes.decorator.AddPvcVolumeDecorator;
import io.dekorate.kubernetes.decorator.AddReadinessProbeDecorator;
import io.dekorate.kubernetes.decorator.AddSecretVolumeDecorator;
import io.dekorate.kubernetes.decorator.AddSidecarDecorator;
import io.dekorate.kubernetes.decorator.AddToSelectorDecorator;
import io.dekorate.kubernetes.decorator.AddVcsUrlAnnotationDecorator;
import io.dekorate.kubernetes.decorator.ApplyArgsDecorator;
import io.dekorate.kubernetes.decorator.ApplyCommandDecorator;
import io.dekorate.kubernetes.decorator.ApplyImagePullPolicyDecorator;
import io.dekorate.kubernetes.decorator.ApplyLimitsCpuDecorator;
import io.dekorate.kubernetes.decorator.ApplyLimitsMemoryDecorator;
import io.dekorate.kubernetes.decorator.ApplyRequestsCpuDecorator;
import io.dekorate.kubernetes.decorator.ApplyRequestsMemoryDecorator;
import io.dekorate.kubernetes.decorator.ApplyServiceAccountNamedDecorator;
import io.dekorate.utils.Labels;
import io.dekorate.utils.Probes;
import io.dekorate.utils.Strings;

import java.util.Arrays;

/**
 * An abstract generator.
 * A generator is meant to populate the initial resources to the {@link Session} as well as adding decorator etc.
 * @param <C>   The config type (its expected to vary between processors).
 */
public abstract class AbstractKubernetesHandler<C extends BaseConfig> implements Handler<C> {

  protected final Resources resources;

  public AbstractKubernetesHandler(Resources resources) {
    this.resources = resources;
  }

  /**
   * Generate / populate the resources.
   * @param config
   */
  public abstract void handle(C config);


  /**
   * Add all decorator to the resources.
   * This method will read the config and then add all the required decorator to the resources.
   * The method is intended to be called from the generate method and thus marked as protected.
   * @param group     The group..
   * @param config    The config.
   */
  protected void addDecorators(String group, C config) {
    if (Strings.isNotNullOrEmpty(config.getServiceAccount())) {
      resources.decorate(new ApplyServiceAccountNamedDecorator(config.getName(), config.getServiceAccount()));
    }

    if (config.getImagePullPolicy() != ImagePullPolicy.IfNotPresent) {
      resources.decorate(group, new ApplyImagePullPolicyDecorator(config.getImagePullPolicy()));
    }

    for (String imagePullSecret: config.getImagePullSecrets()) {
      resources.decorate(new AddImagePullSecretDecorator(config.getName(), imagePullSecret));
    }

    //Metadata handling
    resources.decorate(new AddVcsUrlAnnotationDecorator());
    resources.decorate(new AddCommitIdAnnotationDecorator());

    Labels.createLabels(config).forEach( (k,v) -> {
            resources.decorate(group, new AddLabelDecorator(new Label(k,v)));
            resources.decorate(group, new AddToSelectorDecorator(k, v));
    });

    for (Annotation annotation : config.getAnnotations()) {
      resources.decorate(new AddAnnotationDecorator(annotation));
    }

    if (Strings.isNotNullOrEmpty(config.getServiceAccount())) {
      resources.decorate(group, new ApplyServiceAccountNamedDecorator(config.getName(), config.getServiceAccount()));
    }

    if (config.getImagePullPolicy() != ImagePullPolicy.IfNotPresent) {
      resources.decorate(group, new ApplyImagePullPolicyDecorator(config.getImagePullPolicy()));
    }

    for (String imagePullSecret: config.getImagePullSecrets()) {
      resources.decorate(group, new AddImagePullSecretDecorator(config.getName(), imagePullSecret));
    }

    for (HostAlias hostAlias : config.getHostAliases()) {
      resources.decorate(new AddHostAliasesDecorator(config.getName(), hostAlias));
    }

    for (Container container : config.getSidecars()) {
      resources.decorate(group, new AddSidecarDecorator(config.getName(), container));
    }
   for (Env env : config.getEnvVars()) {
      resources.decorate(group, new AddEnvVarDecorator(config.getName(), config.getName(), env));
    }
    for (Port port : config.getPorts()) {
      resources.decorate(group, new AddPortDecorator(config.getName(), config.getName(), port));
    }
    for (Mount mount: config.getMounts()) {
      resources.decorate(group, new AddMountDecorator(mount));
    }

    for (SecretVolume volume : config.getSecretVolumes()) {
      validateVolume(volume);
      resources.decorate(group, new AddSecretVolumeDecorator(volume));
    }

    for (ConfigMapVolume volume : config.getConfigMapVolumes()) {
      validateVolume(volume);
      resources.decorate(group, new AddConfigMapVolumeDecorator(volume));
    }

    for (PersistentVolumeClaimVolume volume : config.getPvcVolumes()) {
      resources.decorate(group, new AddPvcVolumeDecorator(volume));
    }

    for (AzureFileVolume volume : config.getAzureFileVolumes()) {
      resources.decorate(group, new AddAzureFileVolumeDecorator(volume));
    }

    for (AzureDiskVolume volume : config.getAzureDiskVolumes()) {
      resources.decorate(group, new AddAzureDiskVolumeDecorator(volume));
    }

    for (AwsElasticBlockStoreVolume volume : config.getAwsElasticBlockStoreVolumes()) {
      resources.decorate(group, new AddAwsElasticBlockStoreVolumeDecorator(volume));
    }

    if (config.getCommand().length > 0) {
      resources.decorate(group, new ApplyCommandDecorator(config.getName(), config.getName(), config.getCommand()));
    }

    if (config.getArguments().length > 0) {
      resources.decorate(group, new ApplyArgsDecorator(config.getName(), config.getName(), config.getArguments()));
    }

    if (Probes.isConfigured(config.getLivenessProbe())) {
      resources.decorate(group, new AddLivenessProbeDecorator(config.getName(), config.getName(), config.getLivenessProbe()));
    }

    if (Probes.isConfigured(config.getReadinessProbe())) {
      resources.decorate(group, new AddReadinessProbeDecorator(config.getName(), config.getName(), config.getReadinessProbe()));
    }

    //Container resources
    if (Strings.isNotNullOrEmpty(config.getLimitResources().getCpu())) {
      resources.decorate(group, new ApplyLimitsCpuDecorator(config.getName(), config.getName(), config.getLimitResources().getCpu()));
    }

    if (Strings.isNotNullOrEmpty(config.getLimitResources().getMemory())) {
      resources.decorate(group, new ApplyLimitsMemoryDecorator(config.getName(), config.getName(), config.getLimitResources().getMemory()));
    }

    if (Strings.isNotNullOrEmpty(config.getRequestResources() .getCpu())) {
      resources.decorate(group, new ApplyRequestsCpuDecorator(config.getName(), config.getName(), config.getRequestResources() .getCpu()));
    }

    if (Strings.isNotNullOrEmpty(config.getRequestResources() .getMemory())) {
      resources.decorate(group, new ApplyRequestsMemoryDecorator(config.getName(), config.getName(), config.getRequestResources() .getMemory()));
    }

  }

  private static void validateVolume(SecretVolume volume) {
    if (Strings.isNullOrEmpty(volume.getVolumeName())) {
      throw new IllegalArgumentException("Secret volume requires volumeName().");
    }
    if (Strings.isNullOrEmpty(volume.getSecretName())) {
      throw new IllegalArgumentException("Secret volume: "+ volume.getVolumeName()+". Missing secret name!");
    }
    if (volume.getDefaultMode() < 0 || volume.getDefaultMode() > 0777) {
      throw new IllegalArgumentException("Secret volume: "+ volume.getVolumeName()+". Illegal defaultMode: "+volume.getDefaultMode()+". Should be between: 0000 and 0777!");
    }
  }

  private static void validateVolume(ConfigMapVolume volume) {
    if (Strings.isNullOrEmpty(volume.getVolumeName())) {
      throw new IllegalArgumentException("ConfigMap volume requires volumeName().");
    }
    if (Strings.isNullOrEmpty(volume.getConfigMapName())) {
      throw new IllegalArgumentException("ConfigMap volume: "+ volume.getVolumeName()+". Missing configmap name!");
    }
    if (volume.getDefaultMode() < 0 || volume.getDefaultMode() > 0777) {
      throw new IllegalArgumentException("ConfigMap volume: "+ volume.getVolumeName()+". Illegal defaultMode: "+volume.getDefaultMode()+". Should be between: 0000 and 0777!");
    }
  }
}
