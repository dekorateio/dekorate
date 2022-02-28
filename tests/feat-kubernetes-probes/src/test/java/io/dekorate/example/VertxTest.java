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

import static io.dekorate.testing.KubernetesResources.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;

class VertxTest {

  @Test
  public void shouldContainService() {
    KubernetesList list = loadGenerated("kubernetes");
    Optional<Service> service = findFirst(list, Service.class);
    assertTrue(service.isPresent());
  }

  @Test
  public void shouldContainProbes() {
    KubernetesList list = loadGenerated("kubernetes");
    Optional<Deployment> deployment = findFirst(list, Deployment.class);
    assertTrue(deployment.isPresent(), "Deployment not found!");

    assertReadinessProbe(deployment.get(), "/readiness", 30, 10);
    assertLivenessProbe(deployment.get(), "/liveness", 31, 11);
    assertStartupProbe(deployment.get(), "/startup", 32, 12);
  }

  private static void assertReadinessProbe(Deployment deployment, String actionPath,
      int periodSeconds, int timeoutSeconds) {
    assertProbe(deployment, Container::getReadinessProbe, actionPath, periodSeconds, timeoutSeconds);
  }

  private static void assertLivenessProbe(Deployment deployment, String actionPath,
      int periodSeconds, int timeoutSeconds) {
    assertProbe(deployment, Container::getLivenessProbe, actionPath, periodSeconds, timeoutSeconds);
  }

  private static void assertStartupProbe(Deployment deployment, String actionPath,
      int periodSeconds, int timeoutSeconds) {
    assertProbe(deployment, Container::getStartupProbe, actionPath, periodSeconds, timeoutSeconds);
  }

  private static void assertProbe(Deployment deployment,
      Function<Container, Probe> probeFunction,
      String actionPath, int periodSeconds, int timeoutSeconds) {

    assertTrue(deployment.getSpec().getTemplate().getSpec().getContainers().stream()
        .map(probeFunction)
        .anyMatch(probe -> actionPath.equals(probe.getHttpGet().getPath())
            && periodSeconds == probe.getPeriodSeconds() && timeoutSeconds == probe.getTimeoutSeconds()));
  }
}
