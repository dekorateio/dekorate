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
package io.dekorate.testing.openshift;

import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.dekorate.testing.WithIntegrationTestConfig;
import io.dekorate.testing.openshift.adapter.OpenshiftIntegrationTestConfigAdapter;
import io.dekorate.testing.openshift.annotation.OpenshiftIntegrationTest;
import io.dekorate.testing.openshift.config.OpenshiftIntegrationTestConfig;
import io.dekorate.testing.openshift.config.OpenshiftIntegrationTestConfigBuilder;

public interface WithOpenshiftIntegrationTest extends WithIntegrationTestConfig {

  OpenshiftIntegrationTestConfigBuilder DEFAULT_INTEGRATION_TEST_CONFIG = new OpenshiftIntegrationTestConfigBuilder()
      .withImageStreamTagTimeout(120000L)
      .withReadinessTimeout(500000L);

  default OpenshiftIntegrationTestConfig getOpenshiftIntegrationTestConfig(ExtensionContext context) {
    OpenshiftIntegrationTestConfigBuilder builder = context.getElement()
        .map(e -> OpenshiftIntegrationTestConfigAdapter.newBuilder(e.getAnnotation(OpenshiftIntegrationTest.class)))
        .orElse(DEFAULT_INTEGRATION_TEST_CONFIG);

    // from user properties
    getDeployEnabledFromProperties().ifPresent(builder::withDeployEnabled);
    getBuildEnabledFromProperties().ifPresent(builder::withBuildEnabled);
    Optional.ofNullable(System.getProperty("dekorate.test.openshift.push.enabled")).map(Boolean::parseBoolean)
        .ifPresent(builder::withPushEnabled);
    Optional.ofNullable(System.getProperty("dekorate.test.openshift.image-stream.timeout")).map(Long::parseLong)
        .ifPresent(builder::withImageStreamTagTimeout);
    getReadinessTimeoutFromProperties().ifPresent(builder::withReadinessTimeout);
    getAdditionalModulesFromProperties().ifPresent(builder::withAdditionalModules);

    return builder.build();
  }
}
