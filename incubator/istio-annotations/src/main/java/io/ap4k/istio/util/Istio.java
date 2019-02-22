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

package io.ap4k.istio.util;

import io.ap4k.kubernetes.config.ConfigKey;
import io.ap4k.kubernetes.config.Configuration;
import io.ap4k.istio.annotation.ProxyConfig;

import java.util.HashMap;

public class Istio {

  public static final ConfigKey<String> CLUSTER_NAME = new ConfigKey<>("CLUSTER_NAME", String.class);

  public static final ConfigKey<Integer> V = new ConfigKey<>("V", Integer.class, 2);
  public static final ConfigKey<String> CONFIG_PATH = new ConfigKey<>("CONFIG_PATH", String.class, "/etc/istio/proxy");
  public static final ConfigKey<String> BINARY_PATH = new ConfigKey<>("BINARY_PATH", String.class, "/usr/local/bin/envoy");

  public static final ConfigKey<String> DISCOVERY_ADDRESS = new ConfigKey<>("DISCOVERY_ADDRESS", String.class);
  public static final ConfigKey<Long> DISCOVERY_REFRESH_DELAY = new ConfigKey<>("DISCOVERY_REFRESH_DELAY", Long.class, 1L);

  public static final ConfigKey<String> ZIPKIN_ADDRESS = new ConfigKey<>("ZIPKIN_ADDRESS", String.class);
  public static final ConfigKey<String> STATSD_UDP_ADDRESS = new ConfigKey<>("STATSD_UDP_ADDRESS", String.class);

  public static final ConfigKey<Long> CONNECT_TIMEOUT = new ConfigKey<>("CONNECT_TIMEOUT", Long.class, 10L);
  public static final ConfigKey<Long> PARENT_SHUTDOWN_DURATION = new ConfigKey<>("PARENT_SHUTDOWN_DURATION", Long.class, 1L);
  public static final ConfigKey<Long> DRAIN_DURATION = new ConfigKey<>("DRAIN_DURATION", Long.class , 45L);

  public static final ConfigKey<Integer> PROXY_ADMIN_PORT = new ConfigKey<>("PROXY_ADMIN_PORT", Integer.class, 15000);

  public static final ConfigKey<String> CONTROL_PLANE_AUTH_POLICY = new ConfigKey<>("PROXY_ADMIN_PORT", String.class);

  public static final ConfigKey<String> ISTIO_VERSION = new ConfigKey<>("ISTIO_VERSION", String.class);

  public static Configuration read(ProxyConfig proxyConfig) {
    Configuration result = new Configuration(null, new HashMap<>());
    result.put(CLUSTER_NAME, proxyConfig.serviceCluster());
    result.put(CONFIG_PATH, proxyConfig.configPath());
    result.put(BINARY_PATH, proxyConfig.binaryPath());
    result.put(DISCOVERY_ADDRESS, proxyConfig.discoveryAddress());
    result.put(ZIPKIN_ADDRESS, proxyConfig.zipkinAddress());
    result.put(STATSD_UDP_ADDRESS, proxyConfig.statsdUdpAddress());
    result.put(DRAIN_DURATION, proxyConfig.drainDuration());
    result.put(PARENT_SHUTDOWN_DURATION, proxyConfig.parentShutdownDuration());
    result.put(PROXY_ADMIN_PORT, proxyConfig.proxyAdminPort());
    result.put(CONTROL_PLANE_AUTH_POLICY, proxyConfig.controlPlaneAuthPolicy());
    return result;
  }

}
