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

package io.dekorate.knative;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.dekorate.utils.Serialization;
import io.dekorate.deps.knative.serving.v1.Service;
import io.dekorate.deps.kubernetes.api.model.Container;
import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;

public class Issue687KnativeVolumeTest {

  @Test
  public void shouldHaveVolumesAndMounts() {
    KubernetesList list = Serialization
        .unmarshalAsList(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/knative.yml"));
    assertNotNull(list);
    Service s = findFirst(list, Service.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(s);
    assertTrue(s.getSpec().getTemplate().getSpec().getVolumes().size() > 0);
    Container c = s.getSpec().getTemplate().getSpec().getContainers().get(0);
    assertNotNull(c);
    assertTrue(c.getVolumeMounts().size() > 0);
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
        .filter(i -> t.isInstance(i))
        .findFirst();
  }
}
