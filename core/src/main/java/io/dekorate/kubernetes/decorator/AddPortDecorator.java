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

import static io.dekorate.ConfigReference.joinProperties;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import io.dekorate.ConfigReference;
import io.dekorate.WithConfigReferences;
import io.dekorate.doc.Description;
import io.dekorate.kubernetes.annotation.Protocol;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.utils.Predicates;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.ContainerBuilder;

/**
 * A decorator that adds a port to all containers.
 */
@Description("Add port to to the specified container(s).")
public class AddPortDecorator extends ApplicationContainerDecorator<ContainerBuilder> implements WithConfigReferences {

  private final Port port;

  public AddPortDecorator(Port port) {
    this(ANY, ANY, port);
  }

  public AddPortDecorator(String deployment, String container, Port port) {
    super(deployment, container);
    this.port = port;
  }

  @Override
  public void andThenVisit(ContainerBuilder container) {
    if (container.buildPorts().stream().anyMatch(Predicates.matches(port))) {
      container.editMatchingPort(Predicates.builderMatches(port))
          .withName(port.getName())
          .withHostPort(null)
          .withContainerPort(port.getContainerPort())
          .withProtocol(port.getProtocol() != null ? port.getProtocol().name() : Protocol.TCP.name())
          .endPort();
    } else {
      container.addNewPort()
          .withName(port.getName())
          .withHostPort(null)
          .withContainerPort(port.getContainerPort())
          .withProtocol(port.getProtocol() != null ? port.getProtocol().name() : Protocol.TCP.name())
          .endPort();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    AddPortDecorator addPortDecorator = (AddPortDecorator) o;
    return Objects.equals(port, addPortDecorator.port);
  }

  @Override
  public int hashCode() {
    return Objects.hash(port);
  }

  @Override
  public List<ConfigReference> getConfigReferences() {
    String property = joinProperties("ports." + port.getName());
    return Arrays.asList(new ConfigReference.Builder(property, new String[] { pathForDeployment(), pathForService() })
        .withValue(port.getContainerPort())
        .withDescription("The port number to use for " + port.getName() + ".")
        .build());
  }

  private String pathForDeployment() {
    String portFilter = ".ports.(name == " + port.getName() + ").containerPort";
    String path = "spec.template.spec.containers." + portFilter;
    if (!Strings.equals(getDeploymentName(), ANY) && !Strings.equals(getContainerName(), ANY)) {
      path = "(metadata.name == " + getDeploymentName() + ").spec.template.spec.containers.(name == "
          + getContainerName() + ")" + portFilter;
    } else if (!Strings.equals(getDeploymentName(), ANY)) {
      path = "(metadata.name == " + getDeploymentName() + ").spec.template.spec.containers." + portFilter;
    } else if (!Strings.equals(getContainerName(), ANY)) {
      path = "spec.template.spec.containers.(name == " + getContainerName() + ")" + portFilter;
    }

    return path;
  }

  private String pathForService() {
    String path = "spec.ports.(name == " + port.getName() + ").targetPort";
    if (!Strings.equals(getDeploymentName(), ANY)) {
      path = "(metadata.name == " + getDeploymentName() + ")." + path;
    }

    return path;
  }
}
