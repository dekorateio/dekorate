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


package io.ap4k.openshift.decorator;

import io.ap4k.deps.kubernetes.api.model.KubernetesListBuilder;
import io.ap4k.doc.Description;
import io.ap4k.openshift.config.OpenshiftConfig;
import io.ap4k.kubernetes.config.Port;
import io.ap4k.kubernetes.decorator.Decorator;
import io.ap4k.utils.Strings;

import java.util.Map;
import java.util.Optional;

import static io.ap4k.utils.Ports.getHttpPort;

@Description("Add a route to the list.")
public class AddRouteDecorator extends Decorator<KubernetesListBuilder> {

  private final OpenshiftConfig config;
  private final Map<String, String> allLabels; //A combination of config and project labels.

  public AddRouteDecorator(OpenshiftConfig config, Map<String, String> allLabels) {
    this.config = config;
    this.allLabels = allLabels;
  }

  public void visit(KubernetesListBuilder list) {
    Optional<Port> p = getHttpPort(config);

    if (!p.isPresent() || !config.isExpose()) {
      return;
    }

    Port port = p.get();
    list.addNewRouteItem()
      .withNewMetadata()
      .withName(config.getName())
      .withLabels(allLabels)
      .endMetadata()
      .withNewSpec()
      .withHost(config.getHost())
      .withPath(port.getPath())
      .withNewTo()
        .withKind("Service")
        .withName(config.getName())
      .endTo()
      .withNewPort()
        .withNewTargetPort(port.getContainerPort())
      .endPort()
      .endSpec()
      .endRouteItem();
  }
}
