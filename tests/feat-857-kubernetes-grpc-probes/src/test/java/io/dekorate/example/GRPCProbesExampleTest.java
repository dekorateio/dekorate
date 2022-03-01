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

import static io.dekorate.testing.KubernetesResources.findFirst;
import static io.dekorate.testing.KubernetesResources.loadGenerated;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.kubernetes.api.model.apps.Deployment;

class GRPCProbesExampleTest {

  @Test
  public void shouldContainProbes() {
    KubernetesList list = loadGenerated("kubernetes");
    Optional<Deployment> deployment = findFirst(list, Deployment.class);
    assertTrue(deployment.isPresent(), "Deployment not found!");

    assertReadinessProbe(deployment.get(), 8000, null);
    assertLivenessProbe(deployment.get(), 8001, "service");
    assertStartupProbe(deployment.get(), 8002, null);
  }

  private static void assertReadinessProbe(Deployment deployment, int port, String service) {
    assertProbe(deployment, Container::getReadinessProbe, port, service);
  }

  private static void assertLivenessProbe(Deployment deployment, int port, String service) {
    assertProbe(deployment, Container::getLivenessProbe, port, service);
  }

  private static void assertStartupProbe(Deployment deployment, int port, String service) {
    assertProbe(deployment, Container::getStartupProbe, port, service);
  }

  private static void assertProbe(Deployment deployment, Function<Container, Probe> probeFunction, int port, String service) {

    assertTrue(deployment.getSpec().getTemplate().getSpec().getContainers().stream()
        .map(probeFunction)
        .anyMatch(probe -> Strings.equals(service, probe.getGrpc().getService())
            && port == probe.getGrpc().getPort()));
  }
}
