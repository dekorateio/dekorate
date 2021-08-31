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

package io.dekorate.kubernetes.configurator;

import java.util.Map;
import java.util.function.Predicate;

import io.dekorate.kubernetes.config.BaseConfigFluent;
import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.kubernetes.config.PortBuilder;
import io.dekorate.utils.Ports;
import io.dekorate.utils.Strings;

public class ApplyPort extends Configurator<BaseConfigFluent<?>> {

  private static final String FALLBACK_PORT_NAME = "http";
  private static final String DEFAULT_PATH = "/";

  private final Port port;
  private final Map<String, Integer> nameMappings;

  public ApplyPort(Port port) {
    this(port, Ports.webPortNameMappings());
  }

  public ApplyPort(Port port, Map<String, Integer> nameMappings) {
    this.port = port;
    this.nameMappings = nameMappings;
  }

  @Override
  public void visit(BaseConfigFluent<?> config) {
    Port updated = Ports.populateHostPort(port);
    Predicate<PortBuilder> matchingPortName = p -> updated.getName().equals(p.getName());
    Predicate<PortBuilder> matchingHostPort = p -> updated.getHostPort() != null
        && updated.getHostPort().equals(p.getHostPort());

    boolean matchFound = false;
    if (config.hasMatchingPort(matchingPortName)) {
      matchFound = true;
      applyPath(config, updated, matchingPortName);
      applyContainerPort(config, updated, matchingPortName);

    }

    if (config.hasMatchingPort(matchingHostPort)) {
      matchFound = true;
      applyPath(config, updated, matchingPortName);
      applyContainerPort(config, updated, matchingPortName);
    }

    if (!matchFound) {
      config.addToPorts(updated);
    }
  }

  private void applyPath(BaseConfigFluent<?> config, Port port) {
    applyPath(config, port, p -> port.getName().equals(p.getName()));
  }

  private void applyPath(BaseConfigFluent<?> config, Port port, Predicate<PortBuilder> matchingPortName) {
    if (Strings.isNotNullOrEmpty(port.getPath()) && !DEFAULT_PATH.equals(port.getPath())) {
      config.editMatchingPort(matchingPortName).withPath(port.getPath()).endPort();
    }
  }

  private void applyContainerPort(BaseConfigFluent<?> config, Port port) {
    applyPath(config, port, p -> port.getName().equals(p.getName()));
  }

  private void applyContainerPort(BaseConfigFluent<?> config, Port port, Predicate<PortBuilder> matchingPortName) {
    if (port.getContainerPort() != 0) {
      config.editMatchingPort(matchingPortName).withContainerPort(port.getContainerPort()).endPort();
    }
  }

  private void applyHostPort(BaseConfigFluent<?> config, Port port) {
    applyPath(config, port, p -> port.getHostPort() != null && port.getHostPort().equals(p.getHostPort()));
  }

  private void applyHostPort(BaseConfigFluent<?> config, Port port, Predicate<PortBuilder> matchingHostPort) {
    if (port.getHostPort() != null && port.getHostPort() != 0 && !config.hasMatchingPort(matchingHostPort)) {
      config.editMatchingPort(matchingHostPort)
          .withHostPort(port.getHostPort())
          .endPort();
    }
  }
}
