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
package io.dekorate.testing;

import java.util.Optional;

public interface WithIntegrationTestConfig {
  default Optional<Boolean> getDeployEnabledFromProperties() {
    return Optional.ofNullable(System.getProperty("dekorate.test.deploy.enabled")).map(Boolean::parseBoolean);
  }

  default Optional<Boolean> getBuildEnabledFromProperties() {
    return Optional.ofNullable(System.getProperty("dekorate.test.build.enabled")).map(Boolean::parseBoolean);
  }

  default Optional<Long> getReadinessTimeoutFromProperties() {
    return Optional.ofNullable(System.getProperty("dekorate.test.readiness.timeout")).map(Long::parseLong);
  }

  default Optional<String[]> getAdditionalModulesFromProperties() {
    return Optional.ofNullable(System.getProperty("dekorate.test.additional-modules")).map(v -> v.split(","));
  }
}
