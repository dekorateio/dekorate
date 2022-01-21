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
package io.dekorate.minikube.config;

import io.dekorate.ConfigurationRegistry;
import io.dekorate.config.DefaultConfiguration;
import io.dekorate.kubernetes.configurator.PopulateNodePort;
import io.dekorate.minikube.configurator.ApplyServiceTypeNodePortConfigurator;
import io.dekorate.project.ApplyProjectInfo;

public class DefaultMinikubeConfigGenerator implements MinikubeConfigGenerator {

  private final ConfigurationRegistry configurationRegistry;

  public DefaultMinikubeConfigGenerator(ConfigurationRegistry configurationRegistry) {
    this.configurationRegistry = configurationRegistry;
    this.configurationRegistry.add(new ApplyProjectInfo(getProject()));
    this.configurationRegistry.add(new ApplyServiceTypeNodePortConfigurator());
    this.configurationRegistry.add(new PopulateNodePort());
    add(new DefaultConfiguration<MinikubeConfig>(MinikubeConfig.newMinikubeConfigBuilderFromDefaults()));
  }

  //  Port detectNodePort() {
  //    return new PortBuilder()
  //        // Don't decide here to apply a fixed hostPort as several ports (HTTP, HTTPS, ...) could be generated for a K8s Service
  //        // The AddServiceResourceDecorator will take care to assign it
  //        //.withHostPort()
  //        .withName("nodeport")
  //        .withHostPort(80)
  //        //        .withContainerPort(extractPortFromProperties())
  //        .withNodePort(30123)
  //        .build();
  //  }

  //    Integer extractPortFromProperties() {
  //      final Object server = getProperties().get("server");
  //      if (server != null && Map.class.isAssignableFrom(server.getClass())) {
  //        final Map<String, Object> serverProperties = (Map<String, Object>) server;
  //        final Object port = serverProperties.get("port");
  //        if (port != null) {
  //          return port instanceof Integer ? (Integer) port : Integer.valueOf(port.toString());
  //        }
  //      }
  //      return 8080;
  //    }

  @Override
  public ConfigurationRegistry getConfigurationRegistry() {
    return this.configurationRegistry;
  }
}
