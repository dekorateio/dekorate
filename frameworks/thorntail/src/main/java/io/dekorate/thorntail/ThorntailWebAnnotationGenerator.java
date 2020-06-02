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
package io.dekorate.thorntail;

import io.dekorate.WithSession;
import io.dekorate.Generator;
import io.dekorate.Session;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.kubernetes.config.PortBuilder;
import io.dekorate.kubernetes.configurator.AddPort;
import io.dekorate.kubernetes.configurator.SetPortPath;

import javax.lang.model.element.Element;
import javax.servlet.annotation.WebServlet;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface ThorntailWebAnnotationGenerator extends Generator, WithSession, ThorntailConfigHolder {
  @Override
  default void add(Element element) {
    // JAX-RS
    ApplicationPath applicationPath = element.getAnnotation(ApplicationPath.class);
    if (applicationPath != null) {
      HashMap<String, Object> map = new HashMap<>();
      map.put(ApplicationPath.class.getName(), new HashMap<String, String>() {{
        put("value", applicationPath.value());
      }});
      add(map);
    }

    if (element.getAnnotation(Path.class) != null) {
      add(Collections.emptyMap());
    }

    // servlet
    if (element.getAnnotation(WebServlet.class) != null) {
      add(Collections.emptyMap());
    }
  }

  @Override
  default void add(Map map) {
    Session session = getSession();
    Port port = detectHttpPort();
    session.configurators().add(new AddPort(port));
    //TODO add support for detecting microprofile-health and setting the liveness/readiness probes

    if (map.containsKey(ApplicationPath.class.getName())) {
      Object o  = map.get(ApplicationPath.class.getName());
      if (o instanceof Map) {
        Map<String, Object> applicationPath = (Map) o; 
          if (applicationPath != null && applicationPath.containsKey("value")) {
            String path = String.valueOf(applicationPath.get("value"));
            session.configurators().add(new SetPortPath(port.getName(), path));
          }
      }
    }
  }

  default Port detectHttpPort() {
    return new PortBuilder().withContainerPort(extractPortFromConfig()).withName("http").build();
  }

  @SuppressWarnings("unchecked")
  default Integer extractPortFromConfig() {
    Map<String, Object> config = getThorntailConfig();
    if (config.containsKey("swarm") && !config.containsKey("thorntail")) {
      config.put("thorntail", config.get("swarm"));
    }
    if (config.containsKey("thorntail") && config.get("thorntail") instanceof Map) {
      Map<String, Object> thorntail = (Map<String, Object>) config.get("thorntail");
      if (thorntail.containsKey("http") && thorntail.get("http") instanceof Map) {
        Map<String, Object> http = (Map<String, Object>) thorntail.get("http");
        if (http.containsKey("port")) {
          Object port = http.get("port");
          if (port instanceof Integer) {
            return (Integer) port;
          }
          return Integer.valueOf(port.toString());
        }
      }
    }
    return 8080;
  }

}
