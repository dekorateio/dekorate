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


package io.ap4k.examples.component;

import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.component.model.Component;
import io.ap4k.utils.Serialization;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.Assert.assertEquals;

public class Issue65Test {

  @Test
  public void shouldContainComponent() {
    KubernetesList list = Serialization.unmarshal(Issue65Test.class.getClassLoader().getResourceAsStream("META-INF/ap4k/component.yml"));
    assertNotNull(list);
    assertEquals(1, list.getItems().size());
    assertEquals("Component", list.getItems().get(0));
    assertEquals(1, ((Component)list.getItems().get(0)).getSpec().getLink().length);
  }
}
