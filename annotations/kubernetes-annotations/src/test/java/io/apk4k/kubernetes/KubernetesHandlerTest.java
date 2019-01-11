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


package io.apk4k.kubernetes;

import io.ap4k.kubernetes.annotation.ImagePullPolicy;
import io.ap4k.kubernetes.annotation.ServiceType;
import io.ap4k.kubernetes.config.Annotation;
import io.ap4k.kubernetes.config.AwsElasticBlockStoreVolume;
import io.ap4k.kubernetes.config.AzureDiskVolume;
import io.ap4k.kubernetes.config.AzureFileVolume;
import io.ap4k.kubernetes.config.ConfigKey;
import io.ap4k.kubernetes.config.ConfigMapVolume;
import io.ap4k.kubernetes.config.Env;
import io.ap4k.kubernetes.config.GitRepoVolume;
import io.ap4k.kubernetes.config.Label;
import io.ap4k.kubernetes.config.Mount;
import io.ap4k.kubernetes.config.PersistentVolumeClaimVolume;
import io.ap4k.kubernetes.config.Port;
import io.ap4k.kubernetes.config.Probe;
import io.ap4k.kubernetes.config.SecretVolume;
import io.ap4k.kubernetes.processor.KubernetesHandler;
import io.ap4k.project.Project;

import io.ap4k.kubernetes.config.KubernetesConfig;
import io.ap4k.kubernetes.config.EditableKubernetesConfig;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class KubernetesHandlerTest {

  @Test
  public void shouldAccpetKubernetesConfig()  {
    KubernetesHandler generator = new KubernetesHandler();
    assertTrue(generator.canHandle(KubernetesConfig.class));
  }

  @Test
  public void shouldAccpetEditableKubernetesConfig()  {
    KubernetesHandler generator = new KubernetesHandler();
    assertTrue(generator.canHandle(EditableKubernetesConfig.class));
  }

  @Test
  public void shouldNotAccpetKubernetesConfigSubclasses()  {
    KubernetesHandler generator = new KubernetesHandler();
    assertFalse(generator.canHandle(KubernetesConfigSubclass.class));
  }

  private class KubernetesConfigSubclass extends KubernetesConfig {
    public KubernetesConfigSubclass(Project project, Map<ConfigKey, Object> attributes, String group, String name, String version, Label[] labels, Annotation[] annotations, Env[] envVars, String workingDir, String[] commands, String[] arguments, int replicas, String serviceAccount, Port[] ports, ServiceType serviceType, PersistentVolumeClaimVolume[] pvcVolumes, SecretVolume[] secretVolumes, ConfigMapVolume[] configMapVolumes, GitRepoVolume[] gitRepoVolumes, AwsElasticBlockStoreVolume[] awsElasticBlockStoreVolumes, AzureDiskVolume[] azureDiskVolumes, AzureFileVolume[] azureFileVolumes, Mount[] mounts, ImagePullPolicy imagePullPolicy, Probe livenessProbe, Probe readinessProbe, Boolean autodeployEnabled) {
      super(project, attributes, group, name, version, labels, annotations, envVars, workingDir, commands, arguments, replicas, serviceAccount, ports, serviceType, pvcVolumes, secretVolumes, configMapVolumes, gitRepoVolumes, awsElasticBlockStoreVolumes, azureDiskVolumes, azureFileVolumes, mounts, imagePullPolicy, livenessProbe, readinessProbe, autodeployEnabled);
    }
  }
}
