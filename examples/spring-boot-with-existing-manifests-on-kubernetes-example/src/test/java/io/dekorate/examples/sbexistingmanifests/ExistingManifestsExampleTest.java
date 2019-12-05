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
package io.dekorate.examples.sbexistingmanifests;

import java.util.Optional;

import io.dekorate.deps.kubernetes.api.model.Container;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.deps.kubernetes.api.model.batch.Job;
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
    final Optional<Container> container = job.get().getSpec().getTemplate().getSpec().getContainers().stream()
      .filter(c -> c.getName().equals("countdown"))
      .filter(c -> c.getImage().equals("alpine:3.10"))
      .findAny();
    assertTrue(container.isPresent());
  }
}
