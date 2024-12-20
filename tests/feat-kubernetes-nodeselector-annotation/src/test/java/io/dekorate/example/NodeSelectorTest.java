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

import io.dekorate.utils.Serialization;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NodeSelectorTest {

  @Test
  public void shouldContainNodeSelector() {
    KubernetesList list = Serialization
        .unmarshalAsList(
          NodeSelectorTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    assertNotNull(list);

    Deployment d = findFirst(list, Deployment.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(d);

    PodSpec p = d.getSpec().getTemplate().getSpec();
    assertNotNull(p);

    Map<String, String> ns = p.getNodeSelector();
    assertNotNull(ns);

    assertTrue(ns.containsKey("diskType"));
    assertEquals("ssd", ns.get("diskType"));
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
        .filter(i -> t.isInstance(i))
        .findFirst();
  }
}
