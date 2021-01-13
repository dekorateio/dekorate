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
package io.dekorate.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.dekorate.halkyon.model.Capabilities;
import io.dekorate.halkyon.model.Component;
import io.dekorate.halkyon.model.ComponentCapability;
import io.dekorate.halkyon.model.Parameter;
import io.dekorate.halkyon.model.RequiredComponentCapability;
import io.dekorate.utils.Serialization;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;

public class ComponentSpringBootExampleTest {

  @Test
  public void shouldContainComponent() {
    KubernetesList list = Serialization.unmarshalAsList(
        ComponentSpringBootExampleTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/halkyon.yml"));
    assertNotNull(list);
    List<HasMetadata> items = list.getItems();
    Assertions.assertEquals(1, items.size());
    Component component = (Component) items.get(0);
    Assertions.assertEquals("Component", component.getKind());
    assertEquals("feat-452-halkyon-requirements-with-annotations", component.getSpec().getBuildConfig().getModuleDirName());
    Capabilities capabilities = component.getSpec().getCapabilities();

    RequiredComponentCapability[] requires = capabilities.getRequires();
    assertEquals("db", requires[0].getName());
    assertEquals("postgres-db", requires[0].getBoundTo());
    assertEquals("database", requires[0].getSpec().getCategory());
    assertEquals("postgres", requires[0].getSpec().getType());
    assertTrue(requires[0].isAutoBindable());
    Parameter[] parameters = requires[0].getSpec().getParameters();
    assertEquals(1, parameters.length);
    Parameter parameter = parameters[0];
    assertEquals("postgres-name", parameter.getName());
    assertEquals("postgres-value", parameter.getValue());

    ComponentCapability[] provides = capabilities.getProvides();
    assertEquals("hello-world-endpoint", provides[0].getName());
    assertEquals("api", provides[0].getSpec().getCategory());
    assertEquals("rest-component", provides[0].getSpec().getType());
    assertEquals("1", provides[0].getSpec().getVersion());
  }

}
