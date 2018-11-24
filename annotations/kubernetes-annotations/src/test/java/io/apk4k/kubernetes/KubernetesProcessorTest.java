/**
 * Copyright 2015 The original authors.
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

package io.apk4k.kubernetes;

import io.ap4k.config.ImagePullPolicy;
import io.ap4k.annotation.ServiceType;
import io.ap4k.config.Annotation;
import io.ap4k.config.AwsElasticBlockStoreVolume;
import io.ap4k.config.AzureDiskVolume;
import io.ap4k.config.AzureFileVolume;
import io.ap4k.config.ConfigKey;
import io.ap4k.config.ConfigMapVolume;
import io.ap4k.config.Env;
import io.ap4k.config.GitRepoVolume;
import io.ap4k.config.Label;
import io.ap4k.config.Mount;
import io.ap4k.config.PersistentVolumeClaimVolume;
import io.ap4k.config.Port;
import io.ap4k.config.Probe;
import io.ap4k.config.SecretVolume;
import io.ap4k.project.Project;
import io.ap4k.kubernetes.KubernetesProcessor;

import io.ap4k.config.KubernetesConfig;
import io.ap4k.config.EditableKubernetesConfig;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class KubernetesProcessorTest {

  @Test
  public void shouldAccpetKubernetesConfig()  {
    KubernetesProcessor generator = new KubernetesProcessor();
    assertTrue(generator.accepts(KubernetesConfig.class));
  }

  @Test
  public void shouldAccpetEditableKubernetesConfig()  {
    KubernetesProcessor generator = new KubernetesProcessor();
    assertTrue(generator.accepts(EditableKubernetesConfig.class));
  }

  @Test
  public void shouldNotAccpetKubernetesConfigSubclasses()  {
    KubernetesProcessor generator = new KubernetesProcessor();
    assertFalse(generator.accepts(KubernetesConfigSubclass.class));
  }

  private class KubernetesConfigSubclass extends KubernetesConfig {
    public KubernetesConfigSubclass(Project project, Map<ConfigKey, Object> attributes, String group, String name, String version, Label[] labels, Annotation[] annotations, Env[] envVars, Port[] ports, ServiceType serviceType, PersistentVolumeClaimVolume[] pvcVolumes, SecretVolume[] secretVolumes, ConfigMapVolume[] configMapVolumes, GitRepoVolume[] gitRepoVolumes, AwsElasticBlockStoreVolume[] awsElasticBlockStoreVolumes, AzureDiskVolume[] azureDiskVolumes, AzureFileVolume[] azureFileVolumes, Mount[] mounts, ImagePullPolicy imagePullPolicy, Probe livenessProbe, Probe readinessProbe) {
      super(project, attributes, group, name, version, labels, annotations, envVars, ports, serviceType, pvcVolumes, secretVolumes, configMapVolumes, gitRepoVolumes, awsElasticBlockStoreVolumes, azureDiskVolumes, azureFileVolumes, mounts, imagePullPolicy, livenessProbe, readinessProbe);
    }
  }
}
