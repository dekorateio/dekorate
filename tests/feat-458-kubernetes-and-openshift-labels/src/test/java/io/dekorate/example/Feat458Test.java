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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.dekorate.openshift.OpenshiftAnnotations;
import io.dekorate.openshift.OpenshiftLabels;
import io.dekorate.utils.Annotations;
import io.dekorate.utils.Labels;
import io.dekorate.utils.Serialization;
import io.fabric8.knative.serving.v1.Service;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.openshift.api.model.*;

@Disabled
public class Feat458Test {

  @Test
  public void shouldHaveKubernetesAndOpenshiftLabelsInOpenshiftYml() {
    KubernetesList list = Serialization
        .unmarshalAsList(Feat458Test.class.getClassLoader().getResourceAsStream("META-INF/dekorate/openshift.yml"));
    assertNotNull(list);
    DeploymentConfig d = findFirst(list, DeploymentConfig.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(d);
    Map<String, String> labels = d.getMetadata().getLabels();
    assertNotNull(labels);
    assertTrue(labels.containsKey(Labels.NAME));
    assertTrue(labels.containsKey(Labels.VERSION));
    assertTrue(labels.containsKey(OpenshiftLabels.RUNTIME));

    Map<String, String> annotations = d.getMetadata().getAnnotations();
    assertNotNull(annotations);
    assertTrue(annotations.containsKey(Annotations.COMMIT_ID));
    assertTrue(annotations.containsKey(OpenshiftAnnotations.VCS_URL));
    assertFalse(annotations.containsKey(Annotations.VCS_URL));
  }

  @Test
  public void shouldHaveKubernetestLabelsOpenshiftYml() {
    KubernetesList list = Serialization
        .unmarshalAsList(Feat458Test.class.getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    assertNotNull(list);
    Deployment d = findFirst(list, Deployment.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(d);
    Map<String, String> labels = d.getMetadata().getLabels();
    assertNotNull(labels);
    assertTrue(labels.containsKey(Labels.NAME));
    assertTrue(labels.containsKey(Labels.VERSION));
    assertFalse(labels.containsKey(OpenshiftLabels.RUNTIME));
    Map<String, String> annotations = d.getMetadata().getAnnotations();
    assertNotNull(annotations);
    assertTrue(annotations.containsKey(Annotations.COMMIT_ID));
    assertFalse(annotations.containsKey(OpenshiftAnnotations.VCS_URL));
    assertTrue(annotations.containsKey(Annotations.VCS_URL));
  }

  @Test
  public void shouldHaveKubernetestLabelsKnativeYml() {
    KubernetesList list = Serialization
        .unmarshalAsList(Feat458Test.class.getClassLoader().getResourceAsStream("META-INF/dekorate/knative.yml"));
    assertNotNull(list);
    Service s = findFirst(list, Service.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(s);
    Map<String, String> labels = s.getMetadata().getLabels();
    assertNotNull(labels);
    assertTrue(labels.containsKey(Labels.NAME));
    assertTrue(labels.containsKey(Labels.VERSION));
    assertFalse(labels.containsKey(OpenshiftLabels.RUNTIME));
    Map<String, String> annotations = s.getMetadata().getAnnotations();
    assertNotNull(annotations);
    assertTrue(annotations.containsKey(Annotations.COMMIT_ID));
    assertFalse(annotations.containsKey(OpenshiftAnnotations.VCS_URL));
    assertTrue(annotations.containsKey(Annotations.VCS_URL));
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
        .filter(i -> t.isInstance(i))
        .findFirst();
  }
}
