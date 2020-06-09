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

package io.dekorate.openshift.decorator;

import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.dekorate.doc.Description;
import io.dekorate.openshift.config.OpenshiftConfig;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.utils.Labels;
import io.dekorate.utils.Strings;

import java.util.Map;
import java.util.Optional;

import static io.dekorate.utils.Ports.getHttpPort;

@Description("Add a route to the list.")
public class AddRouteDecorator extends Decorator<KubernetesListBuilder> {

  private final OpenshiftConfig config;

  public AddRouteDecorator(OpenshiftConfig config) {
    this.config = config;
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
      .withLabels(Labels.createLabels(config))
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
