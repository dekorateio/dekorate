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

import io.dekorate.component.model.Capability;
import io.dekorate.component.model.Component;
import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.utils.Serialization;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ComponentServiceSpringBootExampleTest {

  @Test
  public void shouldContainComponentService() throws IOException {
    final ClassLoader classLoader = ComponentServiceSpringBootExampleTest.class.getClassLoader();
    final Properties properties = new Properties();
    properties.load(classLoader.getResourceAsStream("app.properties"));

    KubernetesList list = Serialization.unmarshal(classLoader.getResourceAsStream("META-INF/dekorate/component.yml"));
    assertNotNull(list);
    List<HasMetadata> items = list.getItems();
    assertEquals(2, items.size());

    final Component component = items.stream().filter(i -> i instanceof Component).map(i -> (Component) i).findFirst().orElseThrow(RuntimeException::new);
    assertEquals(properties.getProperty("version.spring-boot"), component.getSpec().getVersion());
    assertEquals("spring-boot", component.getSpec().getRuntime());

    final Capability capability = items.stream().filter(i -> i instanceof Capability).map(i -> (Capability) i).findFirst().orElseThrow(RuntimeException::new);
    assertEquals("mysql-instance", capability.getMetadata().getName());
  }
}
