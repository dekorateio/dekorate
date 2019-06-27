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
import io.ap4k.kubernetes.config.Port;
import io.ap4k.kubernetes.config.PortBuilder;

public class Defaults {

  public static final String AGENT_NAME = "jaeger-agent";
  public static final String AGENT_IMAGE = "jaegertracing/jaeger-agent";

  public static final Port[] AGENT_PORTS = new Port[] {
    new PortBuilder().withName("zipkin-compact").withContainerPort(5775).withProtocol(Protocol.UDP).build(),
    new PortBuilder().withName("jaeger-compact").withContainerPort(6831).withProtocol(Protocol.UDP).build(),
    new PortBuilder().withName("jaeger-binary").withContainerPort(6832).withProtocol(Protocol.UDP).build(),
    new PortBuilder().withName("save-config-sampling-strategies").withContainerPort(5778).withProtocol(Protocol.TCP).build()
  };
}
