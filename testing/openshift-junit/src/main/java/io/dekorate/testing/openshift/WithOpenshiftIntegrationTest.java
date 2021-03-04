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

import org.junit.jupiter.api.extension.ExtensionContext;

import io.dekorate.testing.openshift.adapter.OpenshiftIntegrationTestConfigAdapter;
import io.dekorate.testing.openshift.annotation.OpenshiftIntegrationTest;
import io.dekorate.testing.openshift.config.OpenshiftIntegrationTestConfig;
import io.dekorate.testing.openshift.config.OpenshiftIntegrationTestConfigBuilder;

public interface WithOpenshiftIntegrationTest {

  OpenshiftIntegrationTestConfig DEFAULT_OPENSHIFT_INTEGRATION_TEST_CONFIG = new OpenshiftIntegrationTestConfigBuilder()
      .withImageStreamTagTimeout(120000L)
      .withReadinessTimeout(500000L)
      .build();

  default OpenshiftIntegrationTestConfig getOpenshiftIntegrationTestConfig(ExtensionContext context) {
    return context.getElement()
        .map(e -> OpenshiftIntegrationTestConfigAdapter.adapt(e.getAnnotation(OpenshiftIntegrationTest.class)))
        .orElse(DEFAULT_OPENSHIFT_INTEGRATION_TEST_CONFIG);
  }
}
