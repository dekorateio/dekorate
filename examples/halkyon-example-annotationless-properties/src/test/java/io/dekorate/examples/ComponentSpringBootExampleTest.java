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
package io.dekorate.examples;

import java.util.List;

import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.halkyon.model.Component;
import io.dekorate.halkyon.model.DeploymentMode;
import io.dekorate.utils.Serialization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ComponentSpringBootExampleTest {

  @Test
  public void shouldContainComponentAndLink() {
    KubernetesList list = Serialization.unmarshalAsList(ComponentSpringBootExampleTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/halkyon.yml"));
    assertNotNull(list);
    List<HasMetadata> items = list.getItems();
    Assertions.assertEquals(1, items.size());
    Component component = (Component) items.get(0);
    Assertions.assertEquals("Component", component.getKind());
    assertEquals("https://github.com/dekorateio/dekorate.git", component.getSpec().getBuildConfig().getUrl());
    assertEquals("docker", component.getSpec().getBuildConfig().getType());
    assertEquals("halkyon-example-annotationless-properties", component.getSpec().getBuildConfig().getModuleDirName());
    assertEquals(DeploymentMode.build, component.getSpec().getDeploymentMode());
    assertNotNull("", component.getSpec().getBuildConfig().getRef());
  }

}
