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
 * 
**/

package io.dekorate.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.dekorate.utils.Serialization;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.apps.Deployment;

public class FeatOverwriteSidecarContainersTest {

  private static final String NAME = "feat-overwrite-sidecar-containers";

  @Test
  public void shouldContainOnlyOneSidecarContainer() {
    KubernetesList list = Serialization
        .unmarshalAsList(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    assertNotNull(list);
    Deployment s = findFirst(list, Deployment.class).orElseThrow(() -> new IllegalStateException());
    assertEquals(NAME, s.getMetadata().getName());
    assertEquals(2, s.getSpec().getTemplate().getSpec().getContainers().size());
    assertTrue(s.getSpec().getTemplate().getSpec().getContainers().stream().anyMatch(c -> c.getName().equals("foo")
        && c.getWorkingDir().equals("/work")));
    assertTrue(s.getSpec().getTemplate().getSpec().getContainers().stream().anyMatch(c -> c.getName().equals(NAME)));
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
        .filter(i -> t.isInstance(i))
        .findFirst();
  }
}
