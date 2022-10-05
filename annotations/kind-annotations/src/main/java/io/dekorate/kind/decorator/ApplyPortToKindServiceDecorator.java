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

package io.dekorate.kind.decorator;

import io.dekorate.kubernetes.config.Port;
import io.dekorate.kubernetes.decorator.NamedResourceDecorator;
import io.dekorate.utils.Ports;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ServicePortFluent;

public class ApplyPortToKindServiceDecorator extends NamedResourceDecorator<ServicePortFluent> {

  private final Port port;

  public ApplyPortToKindServiceDecorator(String name, Port port) {
    super(name);
    this.port = port;
  }

  @Override
  public void andThenVisit(ServicePortFluent servicePort, ObjectMeta resourceMeta) {
    if (port.getNodePort() > 0) {
      servicePort.withNodePort(port.getNodePort());
    } else {
      servicePort.withNodePort(Ports.calculateNodePort(getName(), port));
    }
  }

}
