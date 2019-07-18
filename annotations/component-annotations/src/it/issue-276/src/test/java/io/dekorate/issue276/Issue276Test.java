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

package io.dekorate.issue276;

import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.component.model.Component;
import io.dekorate.utils.Serialization;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue276Test {

  @Test
  public void shouldExposeServiceAndHaveCorrectPort() {
    KubernetesList list = Serialization.unmarshal(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/component.yml"));
    assertNotNull(list);
    Optional<Component> component = findFirst(list, Component.class);
    assertTrue(component.isPresent());
    assertTrue(component.get().getSpec().isExposeService());
    assertEquals("customName", component.get().getMetadata().getName());
    assertEquals("customName", component.get().getMetadata().getLabels().get("app"));
    assertEquals(9090, (long)component.get().getSpec().getPort());
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
      .filter(i -> t.isInstance(i))
      .findFirst();
  }
}

