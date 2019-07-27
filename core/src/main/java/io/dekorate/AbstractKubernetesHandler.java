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

import io.dekorate.kubernetes.config.Annotation;
import io.dekorate.kubernetes.config.AwsElasticBlockStoreVolume;
import io.dekorate.kubernetes.config.AzureDiskVolume;
import io.dekorate.kubernetes.config.AzureFileVolume;
import io.dekorate.kubernetes.config.ConfigMapVolume;
import io.dekorate.kubernetes.config.Container;
import io.dekorate.kubernetes.config.Env;
import io.dekorate.kubernetes.config.BaseConfig;
import io.dekorate.kubernetes.config.Label;
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
import io.dekorate.kubernetes.decorator.AddImagePullSecretDecorator;
import io.dekorate.kubernetes.decorator.AddLabelDecorator;
import io.dekorate.kubernetes.decorator.AddLivenessProbeDecorator;
import io.dekorate.kubernetes.decorator.AddMountDecorator;
import io.dekorate.kubernetes.decorator.AddPortDecorator;
import io.dekorate.kubernetes.decorator.AddPvcVolumeDecorator;
import io.dekorate.kubernetes.decorator.AddReadinessProbeDecorator;
import io.dekorate.kubernetes.decorator.AddSecretVolumeDecorator;
import io.dekorate.kubernetes.decorator.AddServiceDecorator;
import io.dekorate.kubernetes.decorator.AddSidecarDecorator;
import io.dekorate.kubernetes.decorator.ApplyArgsDecorator;
import io.dekorate.kubernetes.decorator.ApplyCommandDecorator;
import io.dekorate.kubernetes.decorator.ApplyImagePullPolicyDecorator;
import io.dekorate.kubernetes.decorator.ApplyReplicasDecorator;
import io.dekorate.kubernetes.decorator.ApplyServiceAccountDecorator;
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

  protected void setApplicationInfo(C config) {
    resources.setGroup(config.getGroup());
    resources.setName(config.getName());
    resources.setVersion(config.getVersion());
    resources.setLabels(Labels.createLabels(config));
    Arrays.asList(config.getLabels()).forEach(l -> resources.addLabel(l));
  }

  /**
   * Add all decorator to the resources.
   * This method will read the config and then add all the required decorator to the resources.
   * The method is intended to be called from the generate method and thus marked as protected.
   * @param group     The group..
   * @param config    The config.
   */
  protected void addDecorators(String group, C config) {
    if (Strings.isNotNullOrEmpty(config.getServiceAccount())) {
      resources.decorate(new ApplyServiceAccountDecorator(config.getName(), config.getServiceAccount()));
    }
    resources.decorate(group, new ApplyImagePullPolicyDecorator(config.getImagePullPolicy()));

    for (String imagePullSecret: config.getImagePullSecrets()) {
      resources.decorate(new AddImagePullSecretDecorator(config.getName(), imagePullSecret));
    }

    resources.decorate(group, new ApplyReplicasDecorator(config.getName(), config.getReplicas()));
    //Metadata handling
    for (Label label : config.getLabels()) {
      resources.decorate(new AddLabelDecorator(label));
    }
    for (Annotation annotation : config.getAnnotations()) {
      resources.decorate(new AddAnnotationDecorator(annotation));
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

    if (config.getPorts().length > 0) {
      resources.decorate(group, new AddServiceDecorator(config, resources.getLabels()));
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
  }

  private static void validateVolume(SecretVolume volume) {
    if (volume.getDefaultMode() < 0 || volume.getDefaultMode() > 0777) {
      throw new IllegalArgumentException("Secret volume: "+ volume.getVolumeName()+". Illegal defaultMode: "+volume.getDefaultMode()+". Should be between: 0000 and 0777!");
    }
  }

  private static void validateVolume(ConfigMapVolume volume) {
    if (volume.getDefaultMode() < 0 || volume.getDefaultMode() > 0777) {
      throw new IllegalArgumentException("ConfigMap volume: "+ volume.getVolumeName()+". Illegal defaultMode: "+volume.getDefaultMode()+". Should be between: 0000 and 0777!");
    }
  }
}
