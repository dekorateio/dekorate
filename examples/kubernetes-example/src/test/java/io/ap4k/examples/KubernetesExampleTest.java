/**
 * Copyright 2015 The original authors.
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

package io.ap4k.examples;

import io.ap4k.utils.Serialization;
import org.junit.jupiter.api.Test;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

class KubernetesExampleTest {

  @Test
  public void shouldContainDeployment() {
    KubernetesList list = Serialization.unmarshal(KubernetesExampleTest.class.getClassLoader().getResourceAsStream("META-INF/ap4k/kubernetes.yml"));
    assertNotNull(list);
    assertEquals(1, list.getItems().size());
    assertEquals("Deployment", list.getItems().get(0).getKind());
  }
}
