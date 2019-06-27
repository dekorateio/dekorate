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
package io.ap4k.istio.util;

import io.ap4k.istio.config.IstioConfig;
import io.ap4k.utils.Strings;

import java.util.ArrayList;
import java.util.List;

public class IstioProxy {

  /**
   * Get the istio proxy arguments for the specified config.
   * @param config    The istio config.
   * @return          An array containing all arguments.
   */
  public static String[] getArguments(IstioConfig config) {
    List<String> result = new ArrayList<>();
    if (Strings.isNotNullOrEmpty(config.getProxyConfig().getServiceCluster())) {
      result.add(String.format("--serviceCluster %s", config.getProxyConfig().getServiceCluster()));
    }
    if (Strings.isNotNullOrEmpty(config.getProxyConfig().getConfigPath())) {
      result.add(String.format("--configPath %s", config.getProxyConfig().getConfigPath()));
    }
    if (Strings.isNotNullOrEmpty(config.getProxyConfig().getBinaryPath())) {
      result.add(String.format("--binaryPath %s", config.getProxyConfig().getBinaryPath()));
    }
    if (Strings.isNotNullOrEmpty(config.getProxyConfig().getDiscoveryAddress())) {
      result.add(String.format("--discoveryAddress %s", config.getProxyConfig().getDiscoveryAddress()));
    }
    if (config.getProxyConfig().getDiscoveryRefershDelay() > 0) {
      result.add(String.format("--discoveryRefreshDelay %sm0s", config.getProxyConfig().getDiscoveryRefershDelay()));
    }
    if (Strings.isNotNullOrEmpty(config.getProxyConfig().getZipkinAddress())) {
      result.add(String.format("--zipkinAddress %s", config.getProxyConfig().getZipkinAddress()));
    }
    if (config.getProxyConfig().getParentShutdownDuration() > 0) {
      result.add(String.format("--parentShutdownDuration %sm0s", config.getProxyConfig().getParentShutdownDuration()));
    }
    if (config.getProxyConfig().getDrainDuration() > 0) {
      result.add(String.format("--drainDuration %sm0s", config.getProxyConfig().getDrainDuration()));
    }
    if (config.getProxyConfig().getConnectTimeout() > 0) {
      result.add(String.format("--connectTimeout %sm0s", config.getProxyConfig().getConnectTimeout()));
    }
    if (config.getProxyConfig().getParentShutdownDuration() > 0) {
      result.add(String.format("--parentShutdownDuration %sm0s", config.getProxyConfig().getParentShutdownDuration()));
    }
    if (Strings.isNotNullOrEmpty(config.getProxyConfig().getControlPlaneAuthPolicy())) {
      result.add(String.format("--controlPlaneAuthPolicy %s", config.getProxyConfig().getControlPlaneAuthPolicy()));
    }
    return result.toArray(new String[result.size()]);
  }
}
