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
package io.ap4k.jaeger.handler;

import io.ap4k.Handler;
import io.ap4k.Resources;
import io.ap4k.jaeger.config.Collector;
import io.ap4k.jaeger.config.EditableJaegerAgentConfig;
import io.ap4k.jaeger.config.JaegerAgentConfig;
import io.ap4k.kubernetes.annotation.Protocol;
import io.ap4k.kubernetes.config.AnnotationBuilder;
import io.ap4k.kubernetes.config.Configuration;
import io.ap4k.kubernetes.config.ContainerBuilder;
import io.ap4k.kubernetes.config.Port;
import io.ap4k.kubernetes.decorator.AddAnnotationDecorator;
import io.ap4k.kubernetes.decorator.AddSidecarDecorator;
import io.ap4k.utils.Strings;

import static io.ap4k.jaeger.config.Defaults.AGENT_IMAGE;
import static io.ap4k.jaeger.config.Defaults.AGENT_NAME;

public class JaegerAgentHandler implements Handler<JaegerAgentConfig> {

  private final Resources resources;

  public JaegerAgentHandler(Resources resources) {
    this.resources = resources;
  }

  @Override
  public int order() {
    return 460;
  }

  @Override
  public void handle(JaegerAgentConfig config) {
    if (config.isOperatorEnabled()) {
      resources.decorate(new AddAnnotationDecorator(resources.getName(), new AnnotationBuilder()
        .withKey("sidecar.jaegertracing.io/inject")
        .withValue("true")
        .build()));
    } else {
      ContainerBuilder builder = new ContainerBuilder()
        .withName(AGENT_NAME)
        .withImage(AGENT_IMAGE + ":" + config.getVersion())
        .withArguments("--collector.host-port="+ collectorHostPort(config));

      for (Port port : config.getPorts()) {
        //We can't use the AddPortToContainerDecorator as it expects to be applies on a top level resource.
        builder = builder.addNewPort()
            .withName(port.getName())
            .withHostPort(port.getHostPort() > 0 ? port.getHostPort() : 0)
            .withContainerPort(port.getContainerPort())
            .withProtocol(port.getProtocol() != null ? port.getProtocol() : Protocol.TCP)
          .endPort();
       }
      resources.decorate(new AddSidecarDecorator(resources.getName(), builder.build()));
    }
  }

  /**
   * Create the collector host-port based on the specified {@link JaegerAgentConfig}.
   * @param config  The config.
   * @return        A string with the full host-port.
   */
  private static String collectorHostPort(JaegerAgentConfig config) {
      StringBuilder sb = new StringBuilder();
      Collector collector = config.getCollector();
      if (Strings.isNotNullOrEmpty(collector.getHost())) {
        sb.append(collector.getHost());
      } else {
        sb.append(collector.getName());
        if (Strings.isNotNullOrEmpty(collector.getNamespace())) {
          sb.append(".").append(collector.getNamespace());
        }
        sb.append("svc");
      }
      sb.append(":");
      sb.append(collector.getPort());
      return sb.toString();
    }

  @Override
  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(JaegerAgentConfig.class) || type.equals(EditableJaegerAgentConfig.class);
  }
}
