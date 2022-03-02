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
package io.dekorate.testing.knative;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.dekorate.testing.WithIntegrationTestConfig;
import io.dekorate.testing.adapter.KnativeIntegrationTestConfigAdapter;
import io.dekorate.testing.annotation.KnativeIntegrationTest;
import io.dekorate.testing.config.KnativeIntegrationTestConfig;
import io.dekorate.testing.config.KnativeIntegrationTestConfigBuilder;

public interface WithKnativeIntegrationTestConfig extends WithIntegrationTestConfig {

  KnativeIntegrationTestConfigBuilder DEFAULT_INTEGRATION_TEST_CONFIG = new KnativeIntegrationTestConfigBuilder()
      .withReadinessTimeout(500000);

  default KnativeIntegrationTestConfig getKnativeIntegrationTestConfig(ExtensionContext context) {
    KnativeIntegrationTestConfigBuilder builder = context.getElement()
        .map(e -> KnativeIntegrationTestConfigAdapter.newBuilder(e.getAnnotation(KnativeIntegrationTest.class)))
        .orElse(DEFAULT_INTEGRATION_TEST_CONFIG);

    // from user properties
    getDeployEnabledFromProperties().ifPresent(builder::withDeployEnabled);
    getBuildEnabledFromProperties().ifPresent(builder::withBuildEnabled);
    getReadinessTimeoutFromProperties().ifPresent(builder::withReadinessTimeout);
    getAdditionalModulesFromProperties().ifPresent(builder::withAdditionalModules);

    return builder.build();
  }
}
