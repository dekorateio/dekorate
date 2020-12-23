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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.dekorate.utils.Serialization;
import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.deps.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.dekorate.deps.kubernetes.api.model.apiextensions.CustomResourceDefinitionVersion;
import io.dekorate.deps.kubernetes.api.model.apiextensions.JSONSchemaProps;

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
    Optional<CustomResourceDefinitionVersion> v1 = d.getSpec().getVersions().stream().filter(v -> v.getName().equals("v1")).findFirst();
    v1.ifPresent(v -> {
        assertNotNull(v.getSubresources().getScale());
        assertEquals(".spec.size", v.getSubresources().getScale().getSpecReplicasPath());
        assertEquals(".status.size", v.getSubresources().getScale().getStatusReplicasPath());
        assertNotNull(v.getSubresources().getStatus());

        //Let's version that version is marekd as reqired
        Object spec = v.getSchema().getOpenAPIV3Schema().getProperties().get("spec");
        assertNotNull(spec);
        JSONSchemaProps props = (JSONSchemaProps) spec;
        List<String> required = props.getRequired();
        assertTrue(required.contains("version"));
      });
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
        .filter(i -> t.isInstance(i))
        .findFirst();
  }
}

