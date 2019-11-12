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
import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.halkyon.model.Component;
import io.dekorate.halkyon.model.DeploymentMode;
import io.dekorate.halkyon.model.Link;
import io.dekorate.utils.Serialization;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue71Test {

  @Test
  public void shouldContainLink() {
    KubernetesList list = Serialization.unmarshalAsList(Issue71Test.class.getClassLoader().getResourceAsStream("META-INF/dekorate/halkyon.yml"));
    assertNotNull(list);
    List<HasMetadata> items = list.getItems();
    assertEquals(2, items.size());
    Component component = (Component) items.get(0);
    assertEquals("Component", component.getKind());
    assertEquals(DeploymentMode.build, component.getSpec().getDeploymentMode());
    assertEquals(8080, component.getSpec().getPort());
    assertEquals(true, component.getSpec().isExposeService());
    Link link = (Link) items.get(1);
    assertEquals("Link", link.getKind());
    assertEquals(1, link.getSpec().getEnvs().length);
    assertEquals("target", link.getSpec().getComponentName());

  }
}
