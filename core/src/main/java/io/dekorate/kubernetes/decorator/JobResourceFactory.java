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
import java.util.stream.Collectors;

import io.dekorate.AbstractKubernetesManifestGenerator;
import io.dekorate.ResourceFactory;
import io.dekorate.kubernetes.config.BaseConfig;
import io.dekorate.utils.Labels;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;

public class JobResourceFactory implements ResourceFactory {

  private static final String KIND = "Job";

  @Override
  public String kind() {
    return KIND;
  }

  @Override
  public HasMetadata create(AbstractKubernetesManifestGenerator<?> generator, BaseConfig config) {
    return new JobBuilder()
        .withNewMetadata()
        .withName(config.getName())
        // We are adding the labels up front as they might be picked up by auxiliary resources
        .withLabels(Labels.createLabels(config).stream().collect(Collectors.toMap(l -> l.getKey(), l -> l.getValue())))
        .endMetadata()
        .withNewSpec()
        .withNewSelector()
        .withMatchLabels(new HashMap<String, String>())
        .endSelector()
        .withNewTemplate()
        .withNewSpec()
        .withTerminationGracePeriodSeconds(10L)
        .addNewContainer()
        .withName(config.getName())
        .endContainer()
        .endSpec()
        .endTemplate()
        .endSpec().build();
  }
}
