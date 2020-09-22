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

import java.util.*;

import io.dekorate.kubernetes.config.Configuration;

/**
 * Generates resources, based on the detected annotations.
 */
public interface Generator extends SessionHandler {

  /**
   * Get the {@link Configuration} class that is associated with the {@link Generator}.
   * 
   * @return The {@link Configuration} class.
   */
  default Class<? extends Configuration> getConfigType() {
    return null;
  }

  /**
   * Get the string key associated with the generator.
   * This key is used to correlate configuration properties, with the generator.
   * So a generator with key X will be associated configuration property prefix dekorate.X.
   * Example: The `KubernetesApplicationGenerator` is using the key `kubernetes`.
   * The string key.
   */
  default String getKey() {
    return null;
  }

  /**
   * Add a map as the generator annotation configuration.
   */
  void addAnnotationConfiguration(Map map);

  /**
   * Add a map as the generator configuration.
   */
  void addPropertyConfiguration(Map map);

  /**
   * Generate the resources. This method may be called multiple times, but should
   * only generate the resources once.
   */
  default void generate() {
    // do nothing
    getSession().close();
  }

}
