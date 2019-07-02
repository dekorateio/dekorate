/**
 * Copyright 2018 The original authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/


package io.ap4k.annotationless;

import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.api.model.Service;
import io.ap4k.deps.kubernetes.api.model.apps.Deployment;
import io.ap4k.utils.Serialization;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AnnotationLessTest {

  @Test
  public void shouldContainComponent() {
    KubernetesList list = Serialization.unmarshal(AnnotationLessTest.class.getClassLoader().getResourceAsStream("META-INF/ap4k/kubernetes.yml"));
    assertNotNull(list);
    Deployment d = findFirst(list, Deployment.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(d);
    final Map<String, String> labels = d.getMetadata().getLabels();
    assertEquals("bar", labels.get("foo"));
    assertEquals("baz", labels.get("zoo"));
    assertEquals("annotationless", labels.get("group"));

    Service s = findFirst(list, Service.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(s);
    assertTrue(s.getSpec().getPorts().get(0).getPort().equals(8080));
  }


  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
      .filter(i -> t.isInstance(i))
      .findFirst();
  }
}
