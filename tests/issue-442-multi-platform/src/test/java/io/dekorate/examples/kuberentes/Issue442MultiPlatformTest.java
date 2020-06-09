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

package io.dekorate.examples.kubernetes;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.openshift.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.knative.serving.v1.Service;
import io.dekorate.utils.Serialization;
import io.dekorate.utils.Labels;
import java.net.URL;
import java.util.Optional;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class Issue442MultiPlatformTest {

  @Test
  public void shouldHaveCustomNameInOpenshiftYml() {
    KubernetesList list = Serialization.unmarshalAsList(Issue442MultiPlatformTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/openshift.yml"));
    assertNotNull(list);
    DeploymentConfig d = findFirst(list, DeploymentConfig.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(d);
    assertEquals("o-name", d.getMetadata().getName());
    Map<String, String> labels = d.getMetadata().getLabels();
    assertNotNull(labels);
    assertFalse(labels.containsKey(Labels.PART_OF));
  }

  @Test
  public void shouldHaveCustomNameAndVersionInKubernetesYml() {
    KubernetesList list = Serialization.unmarshalAsList(Issue442MultiPlatformTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    assertNotNull(list);
    Deployment d = findFirst(list, Deployment.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(d);
    assertEquals("k-name", d.getMetadata().getName());
    Map<String, String> labels = d.getMetadata().getLabels();
    assertNotNull(labels);
    assertFalse(labels.containsKey(Labels.PART_OF));
    assertEquals("1.0-kube", labels.get(Labels.VERSION));
  }

  @Test
  public void shouldHaveCustomGroupAndVersionInKnativeYml() {
    KubernetesList list = Serialization.unmarshalAsList(Issue442MultiPlatformTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/knative.yml"));
    assertNotNull(list);
    Service s = findFirst(list, Service.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(s);
    Map<String, String> labels = s.getMetadata().getLabels();
    assertNotNull(labels);
    assertEquals("kn-group", labels.get(Labels.PART_OF));
    assertEquals("1.0-knative", labels.get(Labels.VERSION));
  }


  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
      .filter(i -> t.isInstance(i))
      .findFirst();
  }
}
