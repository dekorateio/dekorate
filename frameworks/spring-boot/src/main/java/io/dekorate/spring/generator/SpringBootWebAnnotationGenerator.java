/**
 * Copyright 2018 The original authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.spring.generator;

import java.util.Collections;
import java.util.Map;

import io.dekorate.ConfigurationGenerator;
import io.dekorate.Session;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.kubernetes.config.PortBuilder;
import io.dekorate.kubernetes.configurator.ApplyPort;
import io.dekorate.spring.SpringPropertiesHolder;
import io.dekorate.utils.Ports;

public interface SpringBootWebAnnotationGenerator extends ConfigurationGenerator, SpringPropertiesHolder {

  String DEKORATE_SPRING_WEB_PATH = "dekorate.spring.web.path";

  Map WEB_ANNOTATIONS = Collections.emptyMap();

  @Override
  default void addAnnotationConfiguration(Map map) {
    addConfiguration(map);
  }

  @Override
  default void addPropertyConfiguration(Map map) {
    addConfiguration(map);
  }

  default void addConfiguration(Map map) {
    Session session = getSession();
    Port port = detectHttpPort(map);
    session.getConfigurationRegistry().add(new ApplyPort(port));
  }

  default Port detectHttpPort(Map map) {
    return new PortBuilder()
        .withName("http")
        // Don't decide here to apply a fixed hostPort as several ports (HTTP, HTTPS, ...) could be generated for a K8s Service
        // The AddServiceResourceDecorator will take care to assign it
        //.withHostPort()
        .withContainerPort(extractPortFromProperties())
        .withPath(String.valueOf(map.getOrDefault(DEKORATE_SPRING_WEB_PATH, Ports.DEFAULT_HTTP_PORT_PATH)))
        .build();
  }

  default Integer extractPortFromProperties() {
    final Object server = getSpringProperties().get("server");
    if (server != null && Map.class.isAssignableFrom(server.getClass())) {
      final Map<String, Object> serverProperties = (Map<String, Object>) server;
      final Object port = serverProperties.get("port");
      if (port != null) {
        return port instanceof Integer ? (Integer) port : Integer.valueOf(port.toString());
      }
    }
    return 8080;
  }
}
