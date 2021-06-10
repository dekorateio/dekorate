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

package io.dekorate.annotationless;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.dekorate.utils.Serialization;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.apps.Deployment;

public class Issue331Test {

  @Test
  public void shouldContainCombinedDeployment() {
    KubernetesList list = Serialization
        .unmarshalAsList(Issue331Test.class.getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    assertNotNull(list);
    Deployment d = findFirst(list, Deployment.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(d);
    final Map<String, String> labels = d.getMetadata().getLabels();
    assertEquals("bar", labels.get("foo"));
    assertEquals("baz", labels.get("food"));
    PodSpec podSpec = d.getSpec().getTemplate().getSpec();
    Container container = podSpec.getContainers().get(0);
    assertNotNull(container);
    assertEquals("test", podSpec.getServiceAccount());
    Optional<ContainerPort> httpPort = container.getPorts().stream()
        .filter(p -> p.getName().equals("http") && p.getContainerPort() == 8081).findAny();
    assertTrue(httpPort.isPresent());
    assertTrue(container.getPorts().stream().filter(p -> p.getName().equals("admin-console") && p.getContainerPort() == 9090)
        .findAny().isPresent());
    assertEquals(3, d.getSpec().getReplicas());

  }

  @Test
  public void shouldContainService() {
    KubernetesList list = Serialization
        .unmarshalAsList(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    assertNotNull(list);
    Service service = findFirst(list, Service.class).orElseThrow(IllegalStateException::new);
    assertNotNull(service);
    assertEquals(1, list.getItems().stream().filter(i -> Service.class.isInstance(i)).count());
    assertTrue(service.getSpec().getPorts().stream()
        .filter(p -> p.getName().equals("http") && p.getTargetPort().getIntVal() == 8081).findAny().isPresent());
    assertTrue(service.getSpec().getType().equals("LoadBalancer"));
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
        .filter(i -> t.isInstance(i))
        .findFirst();
  }
}
