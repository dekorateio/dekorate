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
package io.dekorate.utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.dekorate.kubernetes.config.BaseConfig;
import io.dekorate.kubernetes.config.Container;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.kubernetes.config.PortBuilder;
import io.dekorate.kubernetes.config.PortFluent;
import io.fabric8.kubernetes.api.model.ContainerFluent;
import io.fabric8.kubernetes.api.model.ContainerPort;

public class Ports {

  private static final Map<String, Integer> HTTP_PORT_NAMES = Collections.unmodifiableMap(new HashMap<String, Integer>() {
    {
      put("http", 80);
      put("https", 443);
      put("http1", 80);
      put("h2c", 443);
    }
  });

  private static final Map<Integer, Integer> HTTP_PORT_NUMBERS = Collections.unmodifiableMap(new HashMap<Integer, Integer>() {
    {
      put(80, 80);
      put(8080, 80);
      put(443, 443);
      put(8443, 443);
    }
  });

  public static final String DEFAULT_HTTP_PORT_PATH = "/";
  public static final int MIN_PORT_NUMBER = 1;
  public static final int MAX_PORT_NUMBER = 65535;
  public static final int MIN_NODE_PORT_VALUE = 30000;
  public static final int MAX_NODE_PORT_VALUE = 31999;

  public static final Predicate<PortBuilder> PORT_PREDICATE = p -> HTTP_PORT_NAMES.containsKey(p.getName())
      || HTTP_PORT_NAMES.containsKey(p.getName())
      || HTTP_PORT_NUMBERS.containsKey(p.getContainerPort());

  public static final Map<String, Integer> webPortNameMappings() {
    return HTTP_PORT_NAMES;
  }

  public static final List<String> webPortNames() {
    return HTTP_PORT_NAMES.keySet().stream().collect(Collectors.toList());
  }

  public static final Map<Integer, Integer> webPortNumberMappings() {
    return HTTP_PORT_NUMBERS;
  }

  public static final List<Integer> webPortNumbers() {
    return HTTP_PORT_NUMBERS.keySet().stream().collect(Collectors.toList());
  }

  public static Port populateHostPort(Port port) {
    if (!isWebPort(port)) {
      return port;
    }

    if (port.getHostPort() != null && port.getHostPort() > 0) {
      return port;
    }

    if (port.getContainerPort() != null && HTTP_PORT_NUMBERS.containsKey(port.getContainerPort())) {
      return new PortBuilder(port).withHostPort(HTTP_PORT_NUMBERS.get(port.getContainerPort())).build();
    }

    if (port.getName() != null && HTTP_PORT_NAMES.containsKey(port.getName())) {
      return new PortBuilder(port).withHostPort(HTTP_PORT_NAMES.get(port.getName())).build();
    }
    //No match
    return port;
  }

  public static boolean isWebPort(Port port) {
    if (webPortNames().contains(port.getName())) {
      return true;
    }
    if (webPortNumbers().contains(port.getContainerPort())) {
      return true;
    }
    return false;
  }

  public static boolean isWebPort(PortFluent port) {
    if (webPortNames().contains(port.getName())) {
      return true;
    }
    if (webPortNumbers().contains(port.getContainerPort())) {
      return true;
    }
    return false;
  }

  public static boolean isNodePort(Port port) {
    if (port.getNodePort() != null && port.getNodePort() > 30000 && port.getNodePort() < 31999) {
      return true;
    }
    return false;
  }

  public static Optional<ContainerPort> getHttpPort(ContainerFluent<?> container) {
    //If we have a single port, return that no matter what.
    if (container.getPorts().size() == 1) {
      return Optional.of(container.getPorts().get(0));
    }

    //Check the service name
    Optional<ContainerPort> port = container.getPorts().stream().filter(p -> HTTP_PORT_NAMES.containsKey(p.getName()))
        .findFirst();
    if (port.isPresent()) {
      return port;
    }

    port = container.getPorts().stream().filter(p -> HTTP_PORT_NUMBERS.containsKey(p.getHostPort())).findFirst();
    if (port.isPresent()) {
      return port;
    }
    return Optional.empty();
  }

  public static Optional<Port> getHttpPort(Container container) {
    //If we have a single port, return that no matter what.
    if (container.getPorts().length == 1) {
      return Optional.of(container.getPorts()[0]);
    }

    //Check the service name
    Optional<Port> port = Arrays.stream(container.getPorts()).filter(p -> HTTP_PORT_NAMES.containsKey(p.getName()))
        .findFirst();
    if (port.isPresent()) {
      return port;
    }

    port = Arrays.stream(container.getPorts()).filter(p -> HTTP_PORT_NUMBERS.containsKey(p.getHostPort())).findFirst();
    if (port.isPresent()) {
      return port;
    }
    return Optional.empty();
  }

  public static Optional<Port> getHttpPort(BaseConfig config) {
    //If we have a single port, return that no matter what.
    if (config.getPorts().length == 1) {
      return Optional.of(config.getPorts()[0]);
    }

    //Check the service name
    Optional<Port> port = Arrays.stream(config.getPorts()).filter(p -> HTTP_PORT_NAMES.containsKey(p.getName())).findFirst();
    if (port.isPresent()) {
      return port;
    }

    port = Arrays.stream(config.getPorts()).filter(p -> HTTP_PORT_NUMBERS.containsKey(p.getHostPort())).findFirst();
    if (port.isPresent()) {
      return port;
    }
    return Optional.empty();
  }

  /**
   * Given a string, generate a port number within the supplied range
   * The output is always the same (between {@code min} and {@code max})
   * given the same input and it's useful when we need to generate a port number
   * which needs to stay the same but we don't care about the exact value
   */
  private static int getStablePortNumberInRange(String input, int min, int max) {
    if (min < MIN_PORT_NUMBER || max > MAX_PORT_NUMBER) {
      throw new IllegalArgumentException(
          String.format("Port number range must be within [%d-%d]", MIN_PORT_NUMBER, MAX_PORT_NUMBER));
    }

    try {
      byte[] hash = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
      return min + new BigInteger(hash).mod(BigInteger.valueOf(max - min)).intValue();
    } catch (Exception e) {
      throw new RuntimeException("Unable to generate stable port number from input string: '" + input + "'", e);
    }
  }
}
