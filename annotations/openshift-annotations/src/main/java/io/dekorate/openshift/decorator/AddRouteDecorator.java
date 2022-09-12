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

import static io.dekorate.utils.Ports.getHttpPort;
import static io.dekorate.utils.Ports.getPortByFilter;

import java.util.Optional;

import io.dekorate.doc.Description;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.kubernetes.decorator.AddServiceResourceDecorator;
import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.ResourceProvidingDecorator;
import io.dekorate.openshift.config.OpenshiftConfig;
import io.dekorate.utils.Labels;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.openshift.api.model.RouteBuilder;

@Description("Add a route to the list.")
public class AddRouteDecorator extends ResourceProvidingDecorator<KubernetesListBuilder> {

  private final OpenshiftConfig config;

  public AddRouteDecorator(OpenshiftConfig config) {
    this.config = config;
  }

  public void visit(KubernetesListBuilder list) {
    if (!config.getRoute().isExpose()) {
      return;
    }

    Optional<Port> port = getNamedHttpPort(config);
    if (!port.isPresent()) {
      return;
    }

    if (contains(list, "route.openshift.io/v1", "Route", config.getName())) {
      return;
    }

    list.addToItems(new RouteBuilder()
        .withNewMetadata()
        .withName(config.getName())
        .withLabels(Labels.createLabelsAsMap(config, "Route"))
        .endMetadata()
        .withNewSpec()
        .withHost(config.getRoute().getHost())
        .withPath(port.get().getPath())
        .withNewTo()
        .withKind("Service")
        .withName(config.getName())
        .endTo()
        .withNewPort()
        .withNewTargetPort(port.get().getName())
        .endPort()
        .endSpec()
        .build());
  }

  private Optional<Port> getNamedHttpPort(OpenshiftConfig config) {
    String namedPortName = config.getRoute().getTargetPort();
    if (Strings.isNotNullOrEmpty(namedPortName)) {
      Optional<Port> port = getPortByFilter(p -> Strings.equals(p.getName(), namedPortName), config);
      if (port.isPresent()) {
        return port;
      }

      // Set the named port to the one provided by the user even though it was not found in the configured ports.
      return Optional.of(Port.newBuilder().withName(namedPortName).build());
    }

    return getHttpPort(config);
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { AddServiceResourceDecorator.class };
  }
}
