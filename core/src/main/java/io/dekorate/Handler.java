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
package io.dekorate;

import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.kubernetes.config.Configuration;

public interface Handler<C extends Configuration> {

  /**
   * A number indicating the order each handler will be invoked.
   * Lower number will be invoked first.
   * @return  The number used for ordering.
   */
  int order();

  /**
   * Get the string key associated with the handler.
   * This key is used to correlate configuration properties, with the handler.
   * So a handler with key X will be associated configuration property prefix dekorate.X.  
   * Example: The `KubernetesHandler` is using the key `kubernetes`.
   * The string key.
   */
  String getKey();
 
  /**
   * Generate / populate the resources.
   * @param config The config to handle.
   */
  void handle(C config);

  /**
   * The configuration to use if no configuration for the handler has been specified.
   */
  default ConfigurationSupplier<C> getFallbackConfig() {
    return (ConfigurationSupplier<C>) ConfigurationSupplier.empty();
  }

  /**
   * Check if config is accepted.
   * A generator can choose to which config it should react.
   * @param config The specified config class;
   * @returns True if config type is accepted, false otherwise.
   */
  boolean canHandle(Class<? extends Configuration> config);
}
