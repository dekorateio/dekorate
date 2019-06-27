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
package io.ap4k.spring.generator;

import io.ap4k.Generator;
import io.ap4k.WithSession;
import io.ap4k.kubernetes.config.Port;
import io.ap4k.kubernetes.config.PortBuilder;
import io.ap4k.kubernetes.configurator.AddPort;
import io.ap4k.spring.SpringPropertiesHolder;

import java.util.Collections;
import java.util.Map;

public interface SpringBootWebAnnotationGenerator extends Generator, WithSession, SpringPropertiesHolder {

  Map WEB_ANNOTATIONS=Collections.emptyMap();

  @Override
  default void add(Map map) {
    Port port = detectHttpPort();
    session.configurators().add(new AddPort(port));
    //TODO add support for detecting actuator and setting the liveness/readiness probes path from the configured path
  }

  default Port detectHttpPort()  {
    return new PortBuilder().withContainerPort(extractPortFromProperties()).withName("http").build();
  }

  default Integer extractPortFromProperties() {
    if ((getSpringProperties().containsKey("server"))
            && Map.class.isAssignableFrom(getSpringProperties().get("server").getClass())){
      final Map<String, Object> serverProperties = (Map<String, Object>) getSpringProperties().get("server");
      if(serverProperties.containsKey("port")){
        final Object port = serverProperties.get("port");
        if (port instanceof Integer) {
          return (Integer) port;
        }
        return Integer.valueOf(port.toString());
      }
    }
    return 8080;
  }

}
