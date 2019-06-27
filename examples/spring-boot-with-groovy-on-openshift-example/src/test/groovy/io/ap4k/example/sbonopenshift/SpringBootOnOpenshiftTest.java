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
package io.ap4k.example.sbonopenshift;

import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.api.model.Service;
import io.ap4k.deps.openshift.api.model.DeploymentConfig;
import io.ap4k.utils.Serialization;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SpringBootOnOpenshiftTest {
  @Test
  public void shouldContainDeployment() {
    KubernetesList list = Serialization.unmarshal(SpringBootOnOpenshiftTest.class.getClassLoader().getResourceAsStream("META-INF/ap4k/openshift.yml"));
    assertNotNull(list);
    DeploymentConfig dc = findFirst(list, DeploymentConfig.class).orElseThrow(()-> new IllegalStateException());
    assertNotNull(dc);
  }

  @Test
  public void shouldContainService() {
    KubernetesList list = Serialization.unmarshal(SpringBootOnOpenshiftTest.class.getClassLoader().getResourceAsStream("META-INF/ap4k/openshift.yml"));
    assertNotNull(list);
    Service s = findFirst(list, Service.class).orElseThrow(()-> new IllegalStateException());
    assertNotNull(s);
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
      .filter(i -> t.isInstance(i))
      .findFirst();
  }
}
