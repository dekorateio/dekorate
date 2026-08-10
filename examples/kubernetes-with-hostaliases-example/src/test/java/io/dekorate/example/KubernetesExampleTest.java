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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.dekorate.utils.Serialization;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.apps.Deployment;

class KubernetesExampleTest {

  @Test
  public void shouldContainHostAliases() {
    List<String> expectedHostname1 = Collections.singletonList("test.com");
    List<String> expectedHostname2 = Arrays.asList("foo.org", "bar.com");
    KubernetesList list = Serialization
        .unmarshalAsList(KubernetesExampleTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    assertNotNull(list);
    assertEquals(1, list.getItems().size());
    Deployment deployment = (Deployment) list.getItems().get(0);

    assertEquals(2, deployment.getSpec().getTemplate().getSpec().getHostAliases().size());

    assertTrue(deployment.getSpec().getTemplate().getSpec().getHostAliases().stream().filter(a -> a.getIp().equals("10.0.0.1"))
        .findAny().isPresent());
    assertTrue(deployment.getSpec().getTemplate().getSpec().getHostAliases().stream().filter(a -> a.getIp().equals("127.0.0.1"))
        .findAny().isPresent());

    assertTrue(deployment.getSpec().getTemplate().getSpec().getHostAliases().stream()
        .filter(a -> a.getHostnames().containsAll(expectedHostname1)).findAny().isPresent());
    assertTrue(deployment.getSpec().getTemplate().getSpec().getHostAliases().stream()
        .filter(a -> a.getHostnames().containsAll(expectedHostname2)).findAny().isPresent());
  }
}
