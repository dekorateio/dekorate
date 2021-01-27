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
import java.util.stream.Collectors;

import io.dekorate.kubernetes.config.BaseConfigFluent;
import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.kubernetes.config.PortBuilder;
import io.dekorate.utils.Ports;

public class PopulateWebPort extends Configurator<BaseConfigFluent<?>> {

  public Port map(Port port) {
    if (port.getHostPort() > 0) {
      return port;
    }

    if (!Ports.isWebPort(port)) {
      return port;
    }
    return new PortBuilder(port).withHostPort(80).build();
  } 

  @Override
  public void visit(BaseConfigFluent<?> config) {
    Port[] ports = config.buildPorts();
    config.removeFromPorts(ports);
    config.addAllToPorts(Arrays.stream(ports).map(this::map).collect(Collectors.toList()));
  }
}
