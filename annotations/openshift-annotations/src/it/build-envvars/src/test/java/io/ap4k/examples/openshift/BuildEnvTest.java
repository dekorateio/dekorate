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


package io.ap4k.examples.openshift;

import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.openshift.api.model.BuildConfig;
import io.ap4k.utils.Serialization;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Optional;

public class BuildEnvTest {

  @Test
  public void shouldContainEnvVars() {
    KubernetesList list = Serialization.unmarshal(BuildEnvTest.class.getClassLoader().getResourceAsStream("META-INF/ap4k/openshift.yml"));
    assertNotNull(list);
    BuildConfig buildConfig = findFirst(list, BuildConfig.class).orElseThrow(IllegalStateException::new);

    assertFalse(buildConfig.getSpec().getStrategy().getSourceStrategy().getEnv().isEmpty());
    assertEquals("MAVEN_ARGS", buildConfig.getSpec().getStrategy().getSourceStrategy().getEnv().get(0).getName());
    assertEquals("-Popenshift", buildConfig.getSpec().getStrategy().getSourceStrategy().getEnv().get(0).getValue());
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
      .filter(i -> t.isInstance(i))
      .findFirst();
  }
}
