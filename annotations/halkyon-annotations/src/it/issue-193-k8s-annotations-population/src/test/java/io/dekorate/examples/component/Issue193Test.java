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

package io.dekorate.examples.component;

import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.halkyon.model.Component;
import io.dekorate.utils.Serialization;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue193Test {

  @Test
  public void shouldContainComponent() {
    KubernetesList list = Serialization.unmarshal(Issue193Test.class.getClassLoader().getResourceAsStream("META-INF/dekorate/component.yml"));
    assertNotNull(list);
    assertEquals(1, list.getItems().size());
    assertEquals("Component", list.getItems().get(0).getKind());
    Component component = (Component) list.getItems().get(0);
    assertNotNull(component.getMetadata().getAnnotations());
    assertEquals("bar", component.getMetadata().getAnnotations().get("foo"));
  }
}
