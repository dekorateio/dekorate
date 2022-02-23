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

package io.dekorate.example;

import java.util.List;
import java.util.Optional;

import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.dekorate.utils.Serialization;

import static org.junit.jupiter.api.Assertions.*;

class RestApiFrameworklessOnK8sTest {

  @Test
  public void shouldContainDeploymentAndImage() {
    KubernetesList list = Serialization.unmarshalAsList(RestApiFrameworklessOnK8sTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    assertNotNull(list);
    Deployment d = findFirst(list, Deployment.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(d);

    List<Container> containers = d.getSpec().getTemplate().getSpec().getContainers();
    assertEquals(1, containers.size());
    Container container = containers.get(0);
    assertTrue(Strings.isNotNullOrEmpty(container.getImage()));
  }


  @Test
  public void shouldContainService() {
    KubernetesList list = Serialization.unmarshalAsList(RestApiFrameworklessOnK8sTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    assertNotNull(list);
    Service service = findFirst(list, Service.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(service);

    List<ServicePort> ports = service.getSpec().getPorts();
    assertEquals(1, ports.size());
    ServicePort servicePort = ports.get(0);
    assertEquals("http",servicePort.getName());
    assertEquals(80,servicePort.getPort());
  }

  @Test
  @Disabled
  public void shouldContainIngress() {
    KubernetesList list = Serialization.unmarshalAsList(RestApiFrameworklessOnK8sTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    assertNotNull(list);
    Ingress ingress = findFirst(list, Ingress.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(ingress);
  }


  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
      .filter(i -> t.isInstance(i))
      .findFirst();
  }
}
