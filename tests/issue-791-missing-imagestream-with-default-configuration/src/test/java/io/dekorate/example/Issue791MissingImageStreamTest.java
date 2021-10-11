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

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.dekorate.utils.Serialization;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.DeploymentConfig;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue791MissingImageStreamTest {

  @Test
  public void shouldHaveMatchingOutputImageAndTrigger() {
    KubernetesList list = Serialization.unmarshalAsList(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/openshift.yml"));
    assertNotNull(list);
    DeploymentConfig d = findFirst(list, DeploymentConfig.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(d);
    ImageStream is = findFirst(list, ImageStream.class).orElseThrow(() -> new IllegalStateException());
    assertTrue(is.getSpec().getDockerImageRepository().contains("docker.io"));
    assertNotNull(is);
    assertTrue(d.getSpec().getTriggers().stream().filter(t -> t.getImageChangeParams().getFrom().getName().contains(is.getMetadata().getName())).findFirst().isPresent());
  }
  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
        .filter(i -> t.isInstance(i))
        .findFirst();
  }

}
