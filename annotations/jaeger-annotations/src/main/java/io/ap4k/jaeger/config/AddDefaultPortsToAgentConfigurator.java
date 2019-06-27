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
package io.ap4k.jaeger.config;

import io.ap4k.kubernetes.annotation.Protocol;
import io.ap4k.kubernetes.config.Configurator;

public class AddDefaultPortsToAgentConfigurator extends Configurator<JaegerAgentConfigFluent<?>> {
  @Override
  public void visit(JaegerAgentConfigFluent<?> config) {
    if (config.hasPorts()) {
      return;
    }

    config.
      addNewConfigPort().withName("zipkin-compact").withContainerPort(5775).withProtocol(Protocol.UDP).endConfigPort().
      addNewConfigPort().withName("jaeger-compact").withContainerPort(6831).withProtocol(Protocol.UDP).endConfigPort().
      addNewConfigPort().withName("jaeger-binary").withContainerPort(6832).withProtocol(Protocol.UDP).endConfigPort().
      addNewConfigPort().withName("config-rest").withContainerPort(5778).withProtocol(Protocol.TCP).endConfigPort();
  }
}
