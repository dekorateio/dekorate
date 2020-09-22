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

package io.dekorate.annotationless;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.dekorate.utils.Serialization;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;

public class Issue473Test {

  @Test
  public void shouldContainComponent() {
    KubernetesList list = Serialization
        .unmarshalAsList(Issue473Test.class.getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    assertNotNull(list);
    Deployment d = findFirst(list, Deployment.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(d);
    PodSpec p = d.getSpec().getTemplate().getSpec();
    assertNotNull(p);

    Container c = p.getContainers().get(0);
    assertNotNull(c);
    List<EnvVar> envVars = c.getEnv();
    assertNotNull(c.getEnv());
    Map<String, EnvVar> envMap = envVars.stream().collect(Collectors.toMap(e -> e.getName(), e -> e));
    assertNotNull(envMap.get("FOO"));
    assertEquals("metadata.name", envMap.get("FOO").getValueFrom().getFieldRef().getFieldPath());
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
        .filter(i -> t.isInstance(i))
        .findFirst();
  }
}
