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

import io.dekorate.component.model.Component;
import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.utils.Serialization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import io.dekorate.component.model.Component;
import io.dekorate.component.model.DeploymentMode;

public class ComponentSpringBootExampleTest {

  @Test
  public void shouldContainComponentWithDefaultValues() {
    KubernetesList list = Serialization.unmarshal(ComponentSpringBootExampleTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/component.yml"));
    assertNotNull(list);
    List<HasMetadata> items = list.getItems();
    Assertions.assertEquals(1, items.size());
    Component component = (Component) items.get(0);
    Assertions.assertEquals("Component", component.getKind());
    assertEquals("component-example", component.getMetadata().getName());
    assertEquals(1, component.getMetadata().getLabels().size());
    assertNull(component.getSpec().getBuildConfig());
  }

}
