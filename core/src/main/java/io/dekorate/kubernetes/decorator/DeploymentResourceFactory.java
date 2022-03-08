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
package io.dekorate.kubernetes.decorator;

import java.util.HashMap;

import io.dekorate.AbstractKubernetesManifestGenerator;
import io.dekorate.ResourceFactory;
import io.dekorate.kubernetes.config.BaseConfig;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;

public class DeploymentResourceFactory implements ResourceFactory {

  public static final String KIND = "Deployment";

  @Override
  public String kind() {
    return KIND;
  }

  @Override
  public HasMetadata create(AbstractKubernetesManifestGenerator<?> generator, BaseConfig config) {
    return new DeploymentBuilder()
        .withNewMetadata()
        .withName(config.getName())
        .endMetadata()
        .withNewSpec()
        .withReplicas(1)
        .withNewSelector() //We need to have at least an empty selector so that the decorator can work with it.
        .withMatchLabels(new HashMap<String, String>())
        .endSelector()
        .withTemplate(new PodTemplateSpecBuilder()
            .withSpec(new PodSpecBuilder().build())
            .withNewMetadata().endMetadata().build())
        .endSpec().build();
  }
}
