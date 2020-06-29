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

package io.dekorate.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.dekorate.utils.Serialization;
import io.fabric8.knative.serving.v1.Service;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;

class KnativeExampleTest {

  @Test
  public void shouldContainServiceWithPortNamedHttp1() {
    KubernetesList list = Serialization.unmarshalAsList(KnativeExampleTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/knative.yml"));
    assertNotNull(list);
    assertEquals(1, list.getItems().size());
    Service s = findFirst(list, Service.class).orElseThrow(() -> new IllegalStateException("No knative service found!"));
    assertNotNull(s);
    Container c = s.getSpec().getTemplate().getSpec().getContainers().get(0);
    assertEquals("http1", c.getPorts().get(0).getName());

  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
      .filter(i -> t.isInstance(i))
      .findFirst();
  }
}
