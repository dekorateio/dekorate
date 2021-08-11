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

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import io.dekorate.kubernetes.config.BaseConfigFluent;
import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.kubernetes.config.PortBuilder;
import io.dekorate.utils.Strings;

public class ApplyPort extends Configurator<BaseConfigFluent<?>> {

  private static final String FALLBACK_PORT_NAME = "http";
  private static final String DEFAULT_PATH = "/";

  private final Port port;
  private final List<String> names;

  public ApplyPort(Port port, List<String> names) {
    this.port = port;
    this.names = names;
  }

  public ApplyPort(Port port, String... names) {
    this(port, Arrays.asList(names));
  }

  @Override
  public void visit(BaseConfigFluent<?> config) {
    Predicate<PortBuilder> predicate = p -> names.contains(p.getName());
    if (config.hasMatchingPort(predicate)) {
      if (Strings.isNotNullOrEmpty(port.getPath()) && !DEFAULT_PATH.equals(port.getPath())) {
        config.editMatchingPort(predicate)
            .withPath(port.getPath())
            .endPort();
      }
      if (port.getContainerPort() != 0) {
        config.editMatchingPort(predicate)
            .withContainerPort(port.getContainerPort())
            .endPort();
      }
      if ((port.getHostPort() != null) && (port.getHostPort() != 0)) {
        config.editMatchingPort(predicate)
            .withHostPort(port.getHostPort())
            .endPort();
      } /*
         * Delegate to AddServiceResourceDecorator the role to define the hostPort as it is only needed by the Kubernetes service
         * else if (Ports.isWebPort(port)) {
         * config.editMatchingPort(predicate)
         * .withHostPort(80)
         * .endPort();
         * }
         */
    } else {
      String name = names.size() > 0 ? names.get(0) : FALLBACK_PORT_NAME;
      config.addNewPortLike(port)
          .withName(name)
          .withContainerPort(port.getContainerPort())
          .endPort();
    }
  }
}
