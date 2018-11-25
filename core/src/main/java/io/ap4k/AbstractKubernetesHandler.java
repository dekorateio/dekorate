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
package io.ap4k;

import io.ap4k.config.Annotation;
import io.ap4k.config.AwsElasticBlockStoreVolume;
import io.ap4k.config.AzureDiskVolume;
import io.ap4k.config.AzureFileVolume;
import io.ap4k.config.ConfigMapVolume;
import io.ap4k.config.Env;
import io.ap4k.config.KubernetesConfig;
import io.ap4k.config.Label;
import io.ap4k.config.Mount;
import io.ap4k.config.PersistentVolumeClaimVolume;
import io.ap4k.config.Port;
import io.ap4k.config.SecretVolume;
import io.ap4k.decorator.AddAnnotation;
import io.ap4k.decorator.AddAwsElasticBlockStoreVolume;
import io.ap4k.decorator.AddAzureDiskVolume;
import io.ap4k.decorator.AddAzureFileVolume;
import io.ap4k.decorator.AddConfigMapVolume;
import io.ap4k.decorator.AddEnvVar;
import io.ap4k.decorator.AddLabel;
import io.ap4k.decorator.AddLivenessProbe;
import io.ap4k.decorator.AddMount;
import io.ap4k.decorator.AddPort;
import io.ap4k.decorator.AddPvcVolume;
import io.ap4k.decorator.AddReadinessProbe;
import io.ap4k.decorator.AddSecretVolume;
import io.ap4k.decorator.AddService;

/**
 * An abstract generator.
 * A generator is meant to popullate the initial resources to the {@link Session} as well as adding decorator etc.
 * @param <C>   The configuration type (its expected to vary between processors).
 */
public abstract class AbstractKubernetesHandler<C extends KubernetesConfig> implements Handler<C> {

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
   * This method will read the configuration and then add all the required decorator to the resources.
   * The method is intended to be called from the generate method and thus marked as protected.
   * @param group     The group..
   * @param config    The config.
   */
  protected void addVisitors(String group, C config) {
    for (Label label : config.getLabels()) {
      resources.accept(group, new AddLabel(label));
    }
    for (Annotation annotation : config.getAnnotations()) {
      resources.accept(group, new AddAnnotation(annotation));
    }
    for (Env env : config.getEnvVars()) {
      resources.accept(group, new AddEnvVar(env));
    }
    for (Port port : config.getPorts()) {
      resources.accept(group, new AddPort(port));
    }
    for (Mount mount: config.getMounts()) {
      resources.accept(group, new AddMount(mount));
    }

    for (SecretVolume volume : config.getSecretVolumes()) {
      resources.accept(group, new AddSecretVolume(volume));
    }

    for (ConfigMapVolume volume : config.getConfigMapVolumes()) {
      resources.accept(group, new AddConfigMapVolume(volume));
    }

    for (PersistentVolumeClaimVolume volume : config.getPvcVolumes()) {
      resources.accept(group, new AddPvcVolume(volume));
    }

    for (AzureFileVolume volume : config.getAzureFileVolumes()) {
      resources.accept(group, new AddAzureFileVolume(volume));
    }

    for (AzureDiskVolume volume : config.getAzureDiskVolumes()) {
      resources.accept(group, new AddAzureDiskVolume(volume));
    }

    for (AwsElasticBlockStoreVolume volume : config.getAwsElasticBlockStoreVolumes()) {
      resources.accept(group, new AddAwsElasticBlockStoreVolume(volume));
    }

    if (config.getPorts().length > 0) {
      resources.accept(group, new AddService(config));
    }

    resources.accept(group, new AddLivenessProbe(config.getLivenessProbe()));
    resources.accept(group, new AddReadinessProbe(config.getReadinessProbe()));
  }
}
