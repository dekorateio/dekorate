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

import io.dekorate.kubernetes.annotation.ImagePullPolicy;
import io.dekorate.kubernetes.config.Annotation;
import io.dekorate.kubernetes.config.AwsElasticBlockStoreVolume;
import io.dekorate.kubernetes.config.AzureDiskVolume;
import io.dekorate.kubernetes.config.AzureFileVolume;
import io.dekorate.kubernetes.config.BaseConfig;
import io.dekorate.kubernetes.config.ConfigMapVolume;
import io.dekorate.kubernetes.config.Container;
import io.dekorate.kubernetes.config.Env;
import io.dekorate.kubernetes.config.HostAlias;
import io.dekorate.kubernetes.config.Mount;
import io.dekorate.kubernetes.config.PersistentVolumeClaimVolume;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.kubernetes.config.SecretVolume;
import io.dekorate.kubernetes.decorator.AddAnnotationDecorator;
import io.dekorate.kubernetes.decorator.AddAwsElasticBlockStoreVolumeDecorator;
import io.dekorate.kubernetes.decorator.AddAzureDiskVolumeDecorator;
import io.dekorate.kubernetes.decorator.AddAzureFileVolumeDecorator;
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
import io.dekorate.kubernetes.decorator.AddToMatchingLabelsDecorator;
import io.dekorate.kubernetes.decorator.AddToSelectorDecorator;
import io.dekorate.kubernetes.decorator.ApplyArgsDecorator;
import io.dekorate.kubernetes.decorator.ApplyCommandDecorator;
import io.dekorate.kubernetes.decorator.ApplyImagePullPolicyDecorator;
import io.dekorate.kubernetes.decorator.ApplyLimitsCpuDecorator;
import io.dekorate.kubernetes.decorator.ApplyLimitsMemoryDecorator;
import io.dekorate.kubernetes.decorator.ApplyRequestsCpuDecorator;
import io.dekorate.kubernetes.decorator.ApplyRequestsMemoryDecorator;
import io.dekorate.kubernetes.decorator.ApplyServiceAccountNamedDecorator;
import io.dekorate.kubernetes.decorator.RemoveProbesFromInitContainerDecorator;
import io.dekorate.utils.Labels;
import io.dekorate.utils.Probes;
import io.dekorate.utils.Strings;

/**
 * An abstract generator.
 * A generator is meant to populate the initial resources to the {@link Session} as well as adding decorator etc.
 * 
 * @param <C> The config type (its expected to vary between processors).
 */
public abstract class AbstractKubernetesManifestGenerator<C extends BaseConfig> implements ManifestGenerator<C> {

  protected final ResourceRegistry resourceRegistry;

  public AbstractKubernetesManifestGenerator(ResourceRegistry resources) {
    this.resourceRegistry = resources;
  }

  /**
   * Generate / populate the resources.
   * 
   * @param config
   */
  public abstract void generate(C config);

  /**
   * Add all decorator to the resources.
   * This method will read the config and then add all the required decorator to the resources.
   * The method is intended to be called from the generate method and thus marked as protected.
   * 
   * @param group The group..
   * @param config The config.
   */
  protected void addDecorators(String group, C config) {
    if (Strings.isNotNullOrEmpty(config.getServiceAccount())) {
      resourceRegistry.decorate(new ApplyServiceAccountNamedDecorator(config.getName(), config.getServiceAccount()));
    }

    if (config.getImagePullPolicy() != ImagePullPolicy.IfNotPresent) {
      resourceRegistry.decorate(group, new ApplyImagePullPolicyDecorator(config.getImagePullPolicy()));
    }

    for (String imagePullSecret : config.getImagePullSecrets()) {
      resourceRegistry.decorate(new AddImagePullSecretDecorator(config.getName(), imagePullSecret));
    }

    //Metadata handling
    Labels.createLabels(config).forEach(l -> {
      resourceRegistry.decorate(group, new AddLabelDecorator(config.getName(), l));
      resourceRegistry.decorate(group, new AddToSelectorDecorator(config.getName(), l.getKey(), l.getValue()));
      resourceRegistry.decorate(group, new AddToMatchingLabelsDecorator(config.getName(), l.getKey(), l.getValue()));
    });

    for (Annotation annotation : config.getAnnotations()) {
      resourceRegistry.decorate(new AddAnnotationDecorator(config.getName(), annotation));
    }

    if (Strings.isNotNullOrEmpty(config.getServiceAccount())) {
      resourceRegistry.decorate(group, new ApplyServiceAccountNamedDecorator(config.getName(), config.getServiceAccount()));
    }

    if (config.getImagePullPolicy() != ImagePullPolicy.IfNotPresent) {
      resourceRegistry.decorate(group, new ApplyImagePullPolicyDecorator(config.getName(), config.getImagePullPolicy()));
    }

    for (String imagePullSecret : config.getImagePullSecrets()) {
      resourceRegistry.decorate(group, new AddImagePullSecretDecorator(config.getName(), imagePullSecret));
    }

    for (HostAlias hostAlias : config.getHostAliases()) {
      resourceRegistry.decorate(new AddHostAliasesDecorator(config.getName(), hostAlias));
    }

    for (Container container : config.getSidecars()) {
      resourceRegistry.decorate(group, new AddSidecarDecorator(config.getName(), container));
    }
    for (Env env : config.getEnvVars()) {
      resourceRegistry.decorate(group, new AddEnvVarDecorator(config.getName(), config.getName(), env));
    }
    for (Port port : config.getPorts()) {
      resourceRegistry.decorate(group, new AddPortDecorator(config.getName(), config.getName(), port));
    }
    for (Mount mount : config.getMounts()) {
      resourceRegistry.decorate(group, new AddMountDecorator(mount));
    }

    for (SecretVolume volume : config.getSecretVolumes()) {
      validateVolume(volume);
      resourceRegistry.decorate(group, new AddSecretVolumeDecorator(volume));
    }

    for (ConfigMapVolume volume : config.getConfigMapVolumes()) {
      validateVolume(volume);
      resourceRegistry.decorate(group, new AddConfigMapVolumeDecorator(volume));
    }

    for (PersistentVolumeClaimVolume volume : config.getPvcVolumes()) {
      resourceRegistry.decorate(group, new AddPvcVolumeDecorator(volume));
    }

    for (AzureFileVolume volume : config.getAzureFileVolumes()) {
      resourceRegistry.decorate(group, new AddAzureFileVolumeDecorator(volume));
    }

    for (AzureDiskVolume volume : config.getAzureDiskVolumes()) {
      resourceRegistry.decorate(group, new AddAzureDiskVolumeDecorator(volume));
    }

    for (AwsElasticBlockStoreVolume volume : config.getAwsElasticBlockStoreVolumes()) {
      resourceRegistry.decorate(group, new AddAwsElasticBlockStoreVolumeDecorator(volume));
    }

    if (config.getCommand() != null && config.getCommand().length > 0) {
      resourceRegistry.decorate(group, new ApplyCommandDecorator(config.getName(), config.getName(), config.getCommand()));
    }

    if (config.getArguments() != null && config.getArguments().length > 0) {
      resourceRegistry.decorate(group, new ApplyArgsDecorator(config.getName(), config.getName(), config.getArguments()));
    }

    if (Probes.isConfigured(config.getLivenessProbe())) {
      resourceRegistry.decorate(group,
          new AddLivenessProbeDecorator(config.getName(), config.getName(), config.getLivenessProbe()));
    }

    if (Probes.isConfigured(config.getReadinessProbe())) {
      resourceRegistry.decorate(group,
          new AddReadinessProbeDecorator(config.getName(), config.getName(), config.getReadinessProbe()));
    }

    //Container resources
    if (config.getLimitResources() != null) {
      if (Strings.isNotNullOrEmpty(config.getLimitResources().getCpu())) {
        resourceRegistry.decorate(group,
            new ApplyLimitsCpuDecorator(config.getName(), config.getName(), config.getLimitResources().getCpu()));
      }

      if (Strings.isNotNullOrEmpty(config.getLimitResources().getMemory())) {
        resourceRegistry.decorate(group,
            new ApplyLimitsMemoryDecorator(config.getName(), config.getName(), config.getLimitResources().getMemory()));
      }
    }

    if (config.getRequestResources() != null) {
      if (Strings.isNotNullOrEmpty(config.getRequestResources().getCpu())) {
        resourceRegistry.decorate(group,
            new ApplyRequestsCpuDecorator(config.getName(), config.getName(), config.getRequestResources().getCpu()));
      }

      if (Strings.isNotNullOrEmpty(config.getRequestResources().getMemory())) {
        resourceRegistry.decorate(group, new ApplyRequestsMemoryDecorator(config.getName(), config.getName(),
            config.getRequestResources().getMemory()));
      }
    }

    resourceRegistry.decorate(group, new RemoveProbesFromInitContainerDecorator());
  }

  protected static void validateVolume(SecretVolume volume) {
    if (Strings.isNullOrEmpty(volume.getVolumeName())) {
      throw new IllegalArgumentException("Secret volume requires volumeName().");
    }
    if (Strings.isNullOrEmpty(volume.getSecretName())) {
      throw new IllegalArgumentException("Secret volume: " + volume.getVolumeName() + ". Missing secret name!");
    }
    if (volume.getDefaultMode() != null && (volume.getDefaultMode() < 0 || volume.getDefaultMode() > 0777)) {
      throw new IllegalArgumentException("Secret volume: " + volume.getVolumeName() + ". Illegal defaultMode: "
          + volume.getDefaultMode() + ". Should be between: 0000 and 0777!");
    }
  }

  protected static void validateVolume(ConfigMapVolume volume) {
    if (Strings.isNullOrEmpty(volume.getVolumeName())) {
      throw new IllegalArgumentException("ConfigMap volume requires volumeName().");
    }
    if (Strings.isNullOrEmpty(volume.getConfigMapName())) {
      throw new IllegalArgumentException("ConfigMap volume: " + volume.getVolumeName() + ". Missing configmap name!");
    }
    if (volume.getDefaultMode() != null && (volume.getDefaultMode() < 0 || volume.getDefaultMode() > 0777)) {
      throw new IllegalArgumentException("ConfigMap volume: " + volume.getVolumeName() + ". Illegal defaultMode: "
          + volume.getDefaultMode() + ". Should be between: 0000 and 0777!");
    }
  }
}
