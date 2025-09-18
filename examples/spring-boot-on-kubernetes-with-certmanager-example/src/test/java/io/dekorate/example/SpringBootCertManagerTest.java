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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.dekorate.utils.Serialization;
import io.fabric8.certmanager.api.model.v1.Certificate;
import io.fabric8.certmanager.api.model.v1.Issuer;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.apps.Deployment;

public class SpringBootCertManagerTest {

  private static final String EXPECTED_VOLUME_NAME = "volume-certs";
  private static final String EXPECTED_SECRET_NAME = "tls-secret";
  private static final String HTTPS = "HTTPS";

  @Test
  public void shouldContainCertificate() {
    Certificate certificate = findFirst(Certificate.class);

    assertEquals(EXPECTED_SECRET_NAME, certificate.getSpec().getSecretName());
  }

  @Test
  public void shouldContainSelfSignedIssuer() {
    Issuer issuer = findFirst(Issuer.class);
    assertNotNull(issuer.getSpec().getSelfSigned());
  }

  @Test
  public void shouldContainVolumesAndSchemaInProbesShouldBeHttps() {
    Deployment deployment = findFirst(Deployment.class);

    PodSpec podTemplate = deployment.getSpec().getTemplate().getSpec();
    assertTrue(podTemplate.getVolumes().stream()
        .anyMatch(v -> v.getName().equals(EXPECTED_VOLUME_NAME) && v.getSecret().getSecretName().equals(EXPECTED_SECRET_NAME)));
    assertTrue(podTemplate.getContainers().stream()
        .allMatch(c -> c.getVolumeMounts().stream().anyMatch(m -> m.getName().equals(EXPECTED_VOLUME_NAME))));
    assertTrue(podTemplate.getContainers().stream()
        .allMatch(c -> c.getReadinessProbe().getHttpGet().getScheme().equals(HTTPS)));
    assertTrue(podTemplate.getContainers().stream()
        .allMatch(c -> c.getLivenessProbe().getHttpGet().getScheme().equals(HTTPS)));
  }

  <T extends HasMetadata> T findFirst(Class<T> clazz) {
    KubernetesList list = Serialization
        .unmarshalAsList(
            SpringBootCertManagerTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate/kubernetes.yml"));
    assertNotNull(list);

    return (T) list.getItems().stream()
        .filter(clazz::isInstance)
        .findFirst()
        .orElseThrow(IllegalStateException::new);
  }
}
