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

package io.ap4k.kubernetes;

import io.ap4k.kubernetes.config.KubernetesConfig;
import io.ap4k.deps.kubernetes.api.model.LabelSelector;
import io.ap4k.deps.kubernetes.api.model.LabelSelectorBuilder;
import io.ap4k.deps.kubernetes.api.model.PodSpec;
import io.ap4k.deps.kubernetes.api.model.PodSpecBuilder;
import io.ap4k.deps.kubernetes.api.model.PodTemplateSpec;
import io.ap4k.deps.kubernetes.api.model.PodTemplateSpecBuilder;
import io.ap4k.deps.kubernetes.api.model.apps.Deployment;
import io.ap4k.deps.kubernetes.api.model.apps.DeploymentBuilder;


import static io.ap4k.utils.Labels.createLabels;

public class KubernetesResources {


  private static final String IF_NOT_PRESENT = "IfNotPresent";
  private static final String KUBERNETES_NAMESPACE = "KUBERNETES_NAMESPACE";
  private static final String METADATA_NAMESPACE = "metadata.namespace";

  /**
   * Creates a {@link Deployment} for the {@link KubernetesConfig}.
   * @param config   The sesssion.
   * @return          The deployment.
   */
  public static Deployment createDeployment(KubernetesConfig config)  {
    return new DeploymentBuilder()
      .withNewMetadata()
      .withName(config.getName())
      .endMetadata()
      .withNewSpec()
      .withNewReplicas(1)
      .withTemplate(createPodTemplateSpec(config))
      .withSelector(createSelector(config))
      .endSpec()
      .build();
  }


  /**
   * Creates a {@link LabelSelector} that matches the labels for the {@link KubernetesConfig}.
   * @param config   The config.
   * @return          A labels selector.
   */
  public static LabelSelector createSelector(KubernetesConfig config) {
    return new LabelSelectorBuilder()
      .withMatchLabels(createLabels(config))
      .build();
  }


  /**
   * Creates a {@link PodTemplateSpec} for the {@link KubernetesConfig}.
   * @param config   The sesssion.
   * @return          The pod template specification.
   */
  public static PodTemplateSpec createPodTemplateSpec(KubernetesConfig config) {
    return new PodTemplateSpecBuilder()
      .withSpec(createPodSpec(config))
      .withNewMetadata()
      .withLabels(createLabels(config))
      .endMetadata()
      .build();
  }

  /**
   * Creates a {@link PodSpec} for the {@link KubernetesConfig}.
   * @param config   The sesssion.
   * @return          The pod specification.
   */
  public static PodSpec createPodSpec(KubernetesConfig config) {
    return new PodSpecBuilder()
      .addNewContainer()
      .withName(config.getName())
      .withImage(config.getGroup() + "/" + config.getName() + ":" + config.getVersion())
      .withImagePullPolicy(IF_NOT_PRESENT)
      .addNewEnv()
      .withName(KUBERNETES_NAMESPACE)
      .withNewValueFrom()
      .withNewFieldRef(null, METADATA_NAMESPACE)
      .endValueFrom()
      .endEnv()
      .endContainer()
      .build();
  }

}
