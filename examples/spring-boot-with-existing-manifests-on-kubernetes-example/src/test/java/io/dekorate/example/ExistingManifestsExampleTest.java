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

import java.util.Optional;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.dekorate.utils.Serialization;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExistingManifestsExampleTest {

  @Test
  void shouldContainExistingManifestEntries() {
    final KubernetesList list = Serialization.unmarshalAsList(ExistingManifestsExampleTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    assertNotNull(list);
    final Optional<Job> job = list.getItems().stream().filter(Job.class::isInstance).map(Job.class::cast).findAny();
    assertTrue(job.isPresent());
    assertEquals(job.get().getMetadata().getName(), "Example Job");
    assertEquals(job.get().getSpec().getTemplate().getMetadata().getName(), "Example Job");
    Optional<Container> container = job.get().getSpec().getTemplate().getSpec().getContainers().stream()
      .filter(c -> c.getName().equals("countdown"))
      .filter(c -> c.getImage().equals("alpine:3.10"))
      .findAny();
    assertTrue(container.isPresent());


    final Optional<Deployment> deployment = list.getItems().stream().filter(Deployment.class::isInstance).map(Deployment.class::cast).findAny();
    assertTrue(deployment.isPresent());
    container = deployment.get().getSpec().getTemplate().getSpec().getContainers().stream()
      .filter(c -> "spring-boot-with-existing-manifests-example".equals(c.getName()))
      .findAny();
    assertTrue(container.isPresent());
    assertEquals(123, container.get().getSecurityContext().getRunAsUser());
    assertEquals(789, container.get().getSecurityContext().getRunAsGroup());
  }
}
