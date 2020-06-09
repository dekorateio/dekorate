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
package io.dekorate.it.springbootwithspringcloudkubernetes;

import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.dekorate.utils.Serialization;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DemoApplicationTests {

  @Test
  public void shouldContainerServiceAccount() {
    KubernetesList list = Serialization.unmarshalAsList(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    assertNotNull(list);
    ServiceAccount serviceAccount = findFirst(list, ServiceAccount.class).orElseThrow(IllegalStateException::new);
    assertNotNull(serviceAccount);
    assertEquals(1, list.getItems().stream()
      .filter(i -> ServiceAccount.class.isInstance(i)).count());
    //Desrializing rbac resources is broken at the moment: https://github.com/fabric8io/kubernetes-client/issues/1531
    //RoleBinding roleBinding = findFirst(list, RoleBinding.class).orElseThrow(IllegalStateException::new);
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
      .filter(i -> t.isInstance(i))
      .findFirst();
  }
}
