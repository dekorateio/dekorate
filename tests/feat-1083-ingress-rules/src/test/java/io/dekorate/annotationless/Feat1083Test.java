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

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.dekorate.utils.Serialization;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRule;

public class Feat1083Test {

  private static final String APP_NAME = "feat-1083-ingress-rules";
  private static final String PROD_INGRESS_HOST = "prod.svc.url";
  private static final String DEV_INGRESS_HOST = "dev.svc.url";
  private static final String ALT_INGRESS_HOST = "alt.svc.url";
  private static final String PREFIX = "Prefix";
  private static final String HTTP = "http";

  @Test
  public void shouldIngressHaveTlsConfiguration() {
    KubernetesList list = Serialization
        .unmarshalAsList(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    assertNotNull(list);
    Ingress i = findFirst(list, Ingress.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(i);
    assertEquals(3, i.getSpec().getRules().size(), "There are more rules than expected");
    assertTrue(i.getSpec().getRules().stream().anyMatch(this::generatedRuleWasUpdated));
    assertTrue(i.getSpec().getRules().stream().anyMatch(this::newRuleWasAdded));
    assertTrue(i.getSpec().getRules().stream().anyMatch(this::newRuleWasAddedWithCustomService));
  }

  private boolean newRuleWasAddedWithCustomService(IngressRule rule) {
    return ALT_INGRESS_HOST.equals(rule.getHost())
        && rule.getHttp().getPaths().size() == 1
        && rule.getHttp().getPaths().stream().anyMatch(p -> p.getPath().equals("/ea")
            && p.getPathType().equals(PREFIX)
            && p.getBackend().getService().getName().equals("updated-service")
            && p.getBackend().getService().getPort().getName().equals("tcp"));
  }

  private boolean newRuleWasAdded(IngressRule rule) {
    return DEV_INGRESS_HOST.equals(rule.getHost())
        && rule.getHttp().getPaths().size() == 1
        && rule.getHttp().getPaths().stream().anyMatch(p -> p.getPath().equals("/dev")
            && p.getPathType().equals("ImplementationSpecific")
            && p.getBackend().getService().getName().equals(APP_NAME)
            && p.getBackend().getService().getPort().getName().equals(HTTP));
  }

  private boolean generatedRuleWasUpdated(IngressRule rule) {
    return PROD_INGRESS_HOST.equals(rule.getHost())
        && rule.getHttp().getPaths().size() == 2
        && rule.getHttp().getPaths().stream().anyMatch(p -> p.getPath().equals("/prod")
            && p.getPathType().equals(PREFIX)
            && p.getBackend().getService().getName().equals(APP_NAME)
            && p.getBackend().getService().getPort().getName().equals(HTTP));
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
        .filter(i -> t.isInstance(i))
        .findFirst();
  }
}
