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
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.dekorate.kubernetes.annotation.ServiceType;
import io.dekorate.utils.Serialization;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;

class MinikubeExampleTest {

  @Test
  public void shouldContainNodePortService() {
    KubernetesList list = Serialization
        .unmarshalAsList(MinikubeExampleTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/minikube.yml"));
    assertNotNull(list);
    Service service = findFirst(list, Service.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(service);
    assertEquals(ServiceType.NodePort.name(), service.getSpec().getType());

    List<ServicePort> ports = service.getSpec().getPorts();
    assertEquals(1, ports.size());
    ServicePort servicePort = ports.get(0);
    assertNotNull(servicePort.getNodePort());
    assertTrue(servicePort.getNodePort() >= 30000);
    assertTrue(servicePort.getNodePort() <= 31999);
    assertEquals(80, servicePort.getPort());
    assertEquals(8080, servicePort.getTargetPort().getIntVal());
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
        .filter(i -> t.isInstance(i))
        .findFirst();
  }
}
