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

import static io.dekorate.testing.KubernetesResources.findFirst;
import static io.dekorate.testing.KubernetesResources.loadGenerated;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.TLSConfig;

class VertxTest {

  @Test
  public void shouldContainTlsConfig() {
    KubernetesList list = loadGenerated("openshift");
    Optional<Route> route = findFirst(list, Route.class);
    assertTrue(route.isPresent());

    TLSConfig tls = route.get().getSpec().getTls();
    assertNotNull(tls, "TLS configuration was not created!");
    assertEquals("THE CERTIFICATE", tls.getCertificate());
    assertEquals("THE CA CERTIFICATE", tls.getCaCertificate());
    assertEquals("Redirect", tls.getInsecureEdgeTerminationPolicy());
    assertEquals("THE KEY", tls.getKey());
    assertEquals("reencrypt", tls.getTermination());
    assertEquals("THE DESTINATION CERTIFICATE", tls.getDestinationCACertificate());
  }
}
