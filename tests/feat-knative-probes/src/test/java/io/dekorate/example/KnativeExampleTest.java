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

package io.dekorate.example;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.dekorate.utils.Serialization;
import io.fabric8.knative.serving.v1.Service;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;

class KnativeExampleTest {

  @Test
  public void shouldContainProbes() {
    KubernetesList list = Serialization
        .unmarshalAsList(KnativeExampleTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/knative.yml"));
    Service service = findFirst(list, Service.class).orElseThrow(() -> new IllegalStateException("No knative service found!"));
    assertNoProbe(service);
  }

  private static void assertNoProbe(Service service) {
    assertFalse(service.getSpec().getTemplate().getSpec().getContainers().stream()
        .anyMatch(c -> c.getStartupProbe() != null || c.getReadinessProbe() != null || c.getLivenessProbe() != null));
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
        .filter(i -> t.isInstance(i))
        .findFirst();
  }
}
