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
package io.dekorate.kubernetes.configurator;

import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.kubernetes.config.BaseConfigFluent;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.doc.Description;

import java.util.Objects;

@Description("adds a port to all containers.")
public class AddPort extends Configurator<BaseConfigFluent> {

  private final Port port;

  public AddPort(Port port) {
    this.port = port;
  }

  @Override
  public void visit(BaseConfigFluent config) {
    if (!hasPort(config)) {
      config.addToPorts(port);
    }
  }

  /**
   * Check if the {@link io.dekorate.kubernetes.config.BaseConfig} already has port.
   * @param config  The port.
   * @return        True if port with same container port exists.
   */
  private boolean hasPort(BaseConfigFluent config) {
    for (Port p : config.getPorts()) {
      if (p.getContainerPort() == port.getContainerPort())  {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AddPort addPort = (AddPort) o;
    return Objects.equals(port, addPort.port);
  }

  @Override
  public int hashCode() {

    return Objects.hash(port);
  }
}
