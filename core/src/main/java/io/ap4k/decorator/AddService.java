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


package io.ap4k.decorator;

import io.ap4k.deps.kubernetes.api.model.KubernetesListBuilder;
import io.ap4k.config.KubernetesConfig;
import io.ap4k.deps.kubernetes.api.model.ServicePort;
import io.ap4k.config.Port;
import io.ap4k.deps.kubernetes.api.model.ServicePortBuilder;
import io.ap4k.deps.kubernetes.api.model.IntOrString;
import java.util.Arrays;
import java.util.stream.Collectors;
import io.ap4k.utils.Labels;
import io.ap4k.doc.Description;

@Description("Add a service to the list.")
public class AddService extends Decorator<KubernetesListBuilder> {

  private final KubernetesConfig config;

  public AddService (KubernetesConfig config) {
    this.config = config;
  }

  public void visit(KubernetesListBuilder list) {
    list.addNewServiceItem()
      .withNewMetadata()
      .withName(config.getName())
      .endMetadata()
      .withNewSpec()
      .withType(config.getServiceType().name())
      .withSelector(Labels.createLabels(config))
      .withPorts(Arrays.asList(config.getPorts()).stream().map(this::toServicePort).collect(Collectors.toList()))
      .endSpec()
      .endServiceItem();
  }

  private ServicePort toServicePort(Port port) {
    return new ServicePortBuilder()
      .withName(port.getName())
      .withPort(port.getContainerPort())
      .withTargetPort(new IntOrString(port.getHostPort() > 0 ? port.getHostPort() : port.getContainerPort()))
      .build();
  }
}
