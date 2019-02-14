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
package io.ap4k.utils;

import io.ap4k.kubernetes.config.KubernetesConfig;
import io.ap4k.kubernetes.config.Port;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Ports {

  private static final List<String> HTTP_PORT_NAMES = Arrays.asList(new String[]{"http", "https", "web"});
  private static final List<Integer> HTTP_PORT_NUMBERS = Arrays.asList(new Integer[]{80, 443, 8080, 8443});

  public static Optional<Port> getHttpPort(KubernetesConfig config) {
    //If we have a single port, return that no matter what.
    if (config.getPorts().length == 1) {
      return Optional.of(config.getPorts()[0]);
    }

    //Check the service name
    Optional<Port> port = Arrays.stream(config.getPorts()).filter(p -> HTTP_PORT_NAMES.contains(p.getName())).findFirst();
    if (port.isPresent()) {
      return port;
    }

    port = Arrays.stream(config.getPorts()).filter(p -> HTTP_PORT_NUMBERS.contains(p.getHostPort())).findFirst();
    if (port.isPresent()) {
      return port;
    }
    return Optional.empty();
  }
}
