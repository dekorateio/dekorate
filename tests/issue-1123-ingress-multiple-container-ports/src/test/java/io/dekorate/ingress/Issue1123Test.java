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

package io.dekorate.ingress;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.dekorate.utils.Serialization;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRule;

public class Issue1123Test {

  @Test
  public void shouldIngressHaveTlsConfiguration() {
    KubernetesList list = Serialization
        .unmarshalAsList(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    assertNotNull(list);
    Ingress i = findFirst(list, Ingress.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(i);
    assertEquals(1, i.getSpec().getRules().size());
    IngressRule rule = i.getSpec().getRules().get(0);
    assertEquals(1, rule.getHttp().getPaths().size());
    HTTPIngressPath path = rule.getHttp().getPaths().get(0);
    assertEquals("/secured", path.getPath());
    assertEquals("issue-1123-ingress-multiple-container-ports", path.getBackend().getService().getName());
    // from `dekorate.kubernetes.ingress.target-port`
    assertEquals("https", path.getBackend().getService().getPort().getName());
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
        .filter(i -> t.isInstance(i))
        .findFirst();
  }
}
