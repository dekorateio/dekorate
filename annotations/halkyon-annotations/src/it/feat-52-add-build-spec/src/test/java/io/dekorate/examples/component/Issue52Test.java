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


package io.dekorate.examples.component;

import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.component.model.Component;
import io.dekorate.component.model.Link;
import io.dekorate.utils.Serialization;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue52Test {

  @Test
  public void shouldContainBuildConfig() {
    KubernetesList list = Serialization.unmarshal(Issue52Test.class.getClassLoader().getResourceAsStream("META-INF/dekorate/component.yml"));
    assertNotNull(list);
    List<HasMetadata> items = list.getItems();
    assertEquals(1, items.size());
    Component component = (Component) items.get(0);
    assertEquals("Component", component.getKind());
    //As the git repo is being changed to local, only not null value is checked
    assertNotNull(component.getSpec().getBuildConfig().getUrl(), "Git url shouldn't be null.");
    assertEquals("s2i", component.getSpec().getBuildConfig().getType());
    assertEquals("feat-52-add-build-spec", component.getSpec().getBuildConfig().getModuleDirName());
    assertNotNull(component.getSpec().getBuildConfig().getRef(), "Git ref shouldn't be null.");
  }
}
