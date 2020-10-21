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

import io.dekorate.utils.Serialization;
import org.junit.jupiter.api.Test;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.deps.kubernetes.api.model.apps.Deployment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KubernetesExampleTest {

  @Test
  public void shouldContainHostAliases() {
    List<String> expectedHostname1 = Collections.singletonList("test.com");
    List<String> expectedHostname2 = Arrays.asList("foo.org", "bar.com");
    KubernetesList list = Serialization.unmarshalAsList(KubernetesExampleTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    assertNotNull(list);
    assertEquals(1, list.getItems().size());
    Deployment deployment = (Deployment) list.getItems().get(0);

    assertEquals(2, deployment.getSpec().getTemplate().getSpec().getHostAliases().size());
    assertEquals("10.0.0.1", deployment.getSpec().getTemplate().getSpec().getHostAliases().get(0).getIp());
    assertEquals("127.0.0.1", deployment.getSpec().getTemplate().getSpec().getHostAliases().get(1).getIp());

    assertTrue(expectedHostname1.size() == deployment.getSpec().getTemplate().getSpec().getHostAliases().get(0).getHostnames().size() &&
      expectedHostname1.containsAll(deployment.getSpec().getTemplate().getSpec().getHostAliases().get(0).getHostnames()) &&
        deployment.getSpec().getTemplate().getSpec().getHostAliases().get(0).getHostnames().containsAll(expectedHostname1));

    assertTrue(expectedHostname2.size() == deployment.getSpec().getTemplate().getSpec().getHostAliases().get(1).getHostnames().size() &&
      expectedHostname2.containsAll(deployment.getSpec().getTemplate().getSpec().getHostAliases().get(1).getHostnames()) &&
      deployment.getSpec().getTemplate().getSpec().getHostAliases().get(1).getHostnames().containsAll(expectedHostname2));
  }
}
