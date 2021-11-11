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

import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.dekorate.testing.annotation.Inject;
import io.dekorate.testing.annotation.KubernetesIntegrationTest;
import io.dekorate.testing.annotation.Named;
import io.restassured.RestAssured;
import io.restassured.config.ConnectionConfig;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

@KubernetesIntegrationTest
public class SpringBootOnKubernetesIT {

  @Inject
  private KubernetesClient client;

  @Inject
  private KubernetesList list;

  @Inject
  @Named("spring-boot-on-kubernetes-example")
  Pod pod;

  @Test
  public void shouldRespondWithHelloWorld() throws Exception {
    Assertions.assertNotNull(client);
    Assertions.assertNotNull(list);
    System.out.println("Forwarding port");
    Awaitility.await()
      .ignoreExceptions()
      .dontCatchUncaughtExceptions()
      .atMost(30, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS)
      .untilAsserted(() -> {
      System.out.println("Trying... " + pod.getMetadata().getName());
      try (LocalPortForward p = client.pods().withName(pod.getMetadata().getName()).portForward(9090)) { //port matches what is configured in properties file
        assertTrue(p.isAlive());
        RestAssured.config = RestAssured.config().connectionConfig(new ConnectionConfig()
          .closeIdleConnectionsAfterEachResponseAfter(10, TimeUnit.MILLISECONDS)
          .closeIdleConnectionsAfterEachResponse());
        given().relaxedHTTPSValidation().redirects().follow(true).get("http://localhost:" + p.getLocalPort() + "/")
          .then().log().all().body(is("Hello world"));
      }
    });

  }
}
