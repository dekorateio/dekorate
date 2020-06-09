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
package io.dekorate.example.sbonkubernetes;

import java.util.Optional;
import java.util.function.Predicate;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.dekorate.utils.Serialization;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FmpExampleTest {

  @Test
  void shouldHaveFabric8KubernetesManifest() {
    final KubernetesList list = Serialization.unmarshalAsList(FmpExampleTest.class.getClassLoader().getResourceAsStream("META-INF/fabric8/kubernetes.yml"));

    assertNotNull(list);
    assertFalse(list.getItems().isEmpty());
    final Optional<Deployment> deployment = list.getItems().stream()
      .filter(Deployment.class::isInstance)
      .map(Deployment.class::cast)
      .filter(hasLabel("provider", "fabric8"))
      .filter(hasLabel("decorated-by", "dekorate").negate())
      .findAny();
    assertTrue(deployment.isPresent());
  }

  @Test
  void shouldHaveMergedOpenshiftManifests() {
    final KubernetesList list = Serialization.unmarshalAsList(FmpExampleTest.class.getClassLoader().getResourceAsStream("META-INF/fabric8/openshift.yml"));

    assertNotNull(list);
    assertFalse(list.getItems().isEmpty());
    final Optional<DeploymentConfig> deploymentConfig = list.getItems().stream()
      .filter(DeploymentConfig.class::isInstance)
      .map(DeploymentConfig.class::cast)
      .filter(hasLabel("provider", "fabric8"))
      .filter(hasLabel("decorated-by", "dekorate"))
      .findAny();
    assertTrue(deploymentConfig.isPresent());
  }

  private static Predicate<? super HasMetadata> hasLabel(String key, String value) {
    return hasMetadata ->  hasMetadata.getMetadata().getLabels().entrySet().stream()
      .anyMatch(e -> e.getKey().equals(key) && e.getValue().equals(value));
  }
}
