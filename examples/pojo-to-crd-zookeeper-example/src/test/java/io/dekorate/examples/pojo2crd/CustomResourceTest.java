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

package io.dekorate.examples.pojo2crd;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.dekorate.utils.Serialization;
import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.deps.kubernetes.api.model.apiextensions.CustomResourceDefinition;

class CustomResourceTest {

  @Test
  public void shouldContainUserProvidedProbeConfiguration() {
    KubernetesList list = Serialization.unmarshalAsList(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    assertNotNull(list);
    CustomResourceDefinition d = findFirst(list, CustomResourceDefinition.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(d);
    assertEquals("Zookeeper", d.getSpec().getNames().getKind());
    assertEquals("zookeepers", d.getSpec().getNames().getPlural());
    assertEquals("Namespaced", d.getSpec().getScope());
    assertNotNull(d.getSpec().getSubresources().getScale());
    assertEquals(".spec.size", d.getSpec().getSubresources().getScale().getSpecReplicasPath());
    assertEquals(".status.size", d.getSpec().getSubresources().getScale().getStatusReplicasPath());
    assertNotNull(d.getSpec().getSubresources().getStatus());
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
        .filter(i -> t.isInstance(i))
        .findFirst();
  }
}

