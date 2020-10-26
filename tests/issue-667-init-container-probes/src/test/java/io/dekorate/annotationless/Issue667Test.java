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

package io.dekorate.annotationless;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.dekorate.utils.Serialization;
import io.dekorate.kuberentes.deps.api.model.Container;
import io.dekorate.kuberentes.deps.api.model.HasMetadata;
import io.dekorate.kuberentes.deps.api.model.KubernetesList;
import io.dekorate.kuberentes.deps.api.model.apps.Deployment;

class Issue667Test {

  @Test
  public void shouldFindNoProbesInInitContainers() throws Exception {
    KubernetesList list = Serialization.unmarshalAsList(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    Deployment d = findFirst(list, Deployment.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(d);
    Container c = d.getSpec().getTemplate().getSpec().getContainers().get(0);
    assertNotNull(c);
    assertNotNull(c.getLivenessProbe());
    assertNotNull(c.getReadinessProbe());

    Container i = d.getSpec().getTemplate().getSpec().getInitContainers().get(0);
    assertNotNull(i);
    assertNull(i.getLivenessProbe());
    assertNull(i.getReadinessProbe());
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
        .filter(i -> t.isInstance(i))
        .findFirst();
  }
}

