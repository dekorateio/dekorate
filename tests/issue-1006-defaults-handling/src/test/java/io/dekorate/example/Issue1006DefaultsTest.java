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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.dekorate.utils.Serialization;
import io.fabric8.knative.serving.v1.Service;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.apps.Deployment;

public class Issue1006DefaultsTest {

  @Test
  public void shouldHaveOriginalPullPolicy() {
    KubernetesList list = Serialization
        .unmarshalAsList(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    assertNotNull(list);
    Deployment d = findFirst(list, Deployment.class).orElseThrow(() -> new IllegalStateException());
    assertEquals("test-app", d.getMetadata().getName());
    assertEquals("Never", d.getSpec().getTemplate().getSpec().getContainers().get(0).getImagePullPolicy());
  }

  @Test
  public void shouldHaveOriginalKnativeDefaults() {
    KubernetesList list = Serialization
        .unmarshalAsList(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/knative.yml"));
    assertNotNull(list);
    Service s = findFirst(list, Service.class).orElseThrow(() -> new IllegalStateException());
    assertEquals("test-app", s.getMetadata().getName());
    assertEquals("Never", s.getSpec().getTemplate().getSpec().getContainers().get(0).getImagePullPolicy());
    assertEquals(3, s.getSpec().getTemplate().getSpec().getContainerConcurrency());
    assertEquals("10", s.getSpec().getTemplate().getMetadata().getAnnotations().get("autoscaling.knative.dev/min-scale"));
    assertEquals("20", s.getSpec().getTemplate().getMetadata().getAnnotations().get("autoscaling.knative.dev/max-scale"));
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
        .filter(i -> t.isInstance(i))
        .findFirst();
  }

}
