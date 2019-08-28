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

package io.dekorate.annotationless;

import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.deps.kubernetes.api.model.apps.Deployment;
import io.dekorate.deps.kubernetes.api.model.Container;
import io.dekorate.deps.kubernetes.api.model.PodSpec;
import io.dekorate.utils.Serialization;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class Issue331Test {

  @Test
  public void shouldContainCombinedDeployment() {
    KubernetesList list = Serialization.unmarshal(Issue331Test.class.getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    assertNotNull(list);
    Deployment d = findFirst(list, Deployment.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(d);
    PodSpec podSpec = d.getSpec().getTemplate().getSpec();
    Container container = podSpec.getContainers().get(0);
    assertNotNull(container);
    assertEquals("test", podSpec.getServiceAccount());
    assertTrue(container.getPorts().stream().filter(p -> p.getName().equals("http") && p.getContainerPort() == 8080)
        .findAny().isPresent());
  }


  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
      .filter(i -> t.isInstance(i))
      .findFirst();
  }
}
