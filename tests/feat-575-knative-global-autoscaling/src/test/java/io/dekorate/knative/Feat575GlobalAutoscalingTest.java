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

package io.dekorate.knative;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.dekorate.utils.Serialization;
import io.dekorate.deps.kubernetes.api.model.ConfigMap;
import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;

public class Feat575GlobalAutoscalingTest {

  @Test
  public void shouldContainRequestsPerSecond() {
    KubernetesList list = Serialization.unmarshalAsList(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/knative.yml"));
    assertNotNull(list);
    ConfigMap c = findFirst(list, ConfigMap.class).orElseThrow(() -> new IllegalStateException());
    String rps = c.getData().get("requests-per-second-target-default");
    assertEquals("100", rps);
  }


  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
      .filter(i -> t.isInstance(i))
      .findFirst();
  }
}
