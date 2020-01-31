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
package io.dekorate.jaeger.handler;

import io.dekorate.Handler;
import io.dekorate.Resources;
import io.dekorate.jaeger.config.Collector;
import io.dekorate.jaeger.config.EditableJaegerAgentConfig;
import io.dekorate.jaeger.config.JaegerAgentConfig;
import io.dekorate.kubernetes.annotation.Protocol;
import io.dekorate.kubernetes.config.AnnotationBuilder;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.ContainerBuilder;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.kubernetes.decorator.AddAnnotationDecorator;
import io.dekorate.kubernetes.decorator.AddSidecarDecorator;
import io.dekorate.utils.Strings;

import static io.dekorate.jaeger.config.Defaults.AGENT_IMAGE;
import static io.dekorate.jaeger.config.Defaults.AGENT_NAME;

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
      resources.decorate(new AddAnnotationDecorator(new AnnotationBuilder()
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
      resources.decorate(new AddSidecarDecorator(builder.build()));
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
