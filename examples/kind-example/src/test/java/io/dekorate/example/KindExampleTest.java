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

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.dekorate.kubernetes.annotation.ImagePullPolicy;
import io.dekorate.utils.Serialization;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.apps.Deployment;

class KindExampleTest {

  @Test
  public void shouldExpectedConfiguration() {
    KubernetesList kindList = Serialization.unmarshalAsList(KindExampleTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/kind.yml"));
    assertNotNull(kindList);
    Deployment kindDeployment = findFirst(kindList, Deployment.class).orElseThrow(() -> new IllegalStateException("Deployment not found in kind.yml!"));
    assertEquals(ImagePullPolicy.Never.name(), kindDeployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImagePullPolicy());

    KubernetesList kubernetes = Serialization.unmarshalAsList(KindExampleTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    assertNotNull(kubernetes);
    Deployment kubernetesDeployment = findFirst(kubernetes, Deployment.class).orElseThrow(() -> new IllegalStateException("Deployment not found in kubernetes.yml!"));
    assertEquals(ImagePullPolicy.Always.name(), kubernetesDeployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImagePullPolicy());
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
      .filter(i -> t.isInstance(i))
      .findFirst();
  }
}
