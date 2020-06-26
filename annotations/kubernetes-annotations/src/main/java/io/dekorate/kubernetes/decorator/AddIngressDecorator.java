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

import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.dekorate.doc.Description;
import io.dekorate.kubernetes.config.KubernetesConfig;
import io.dekorate.kubernetes.config.Port;

import java.util.Map;
import java.util.Optional;

import static io.dekorate.utils.Ports.getHttpPort;

@Description("Add an ingress to the list.")
public class AddIngressDecorator extends ResourceProvidingDecorator<KubernetesListBuilder> {

  private final KubernetesConfig config;
  private final Map<String, String> allLabels; //A combination of config and project labels.


  public AddIngressDecorator(KubernetesConfig config, Map<String, String> allLabels) {
    this.config = config;
    this.allLabels = allLabels;
  }

  public void visit(KubernetesListBuilder list) {
    Optional<Port> p = getHttpPort(config);
    if (!p.isPresent() || !config.isExpose()) {
      return;
    }
    Port port = p.get();
    list.addNewIngressItem()
      .withNewMetadata()
      .withName(config.getName())
      .withLabels(allLabels)
      .endMetadata()
      .withNewSpec()
      .addNewRule()
      .withHost(config.getHost())
      .withNewHttp()
      .addNewPath()
        .withPath(port.getPath())
        .withNewBackend()
          .withServiceName(config.getName())
          .withNewServicePort(port.getContainerPort())
        .endBackend()
      .endPath()
      .endHttp()
      .endRule()
      .endSpec()
      .endIngressItem();
  }
}
