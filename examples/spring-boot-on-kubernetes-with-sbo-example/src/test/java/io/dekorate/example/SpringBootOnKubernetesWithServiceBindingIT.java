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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import io.dekorate.testing.annotation.Inject;
import io.dekorate.testing.annotation.KubernetesIntegrationTest;
import io.dekorate.testing.annotation.Named;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@KubernetesIntegrationTest
public class SpringBootOnKubernetesWithServiceBindingIT {

  @Inject
  private KubernetesClient client;

  @Inject
  private KubernetesList list;

  @Inject
  @Named("spring-boot-on-kubernetes-with-sbo-example")
  Pod pod;

  @Test
  public void shouldRespondWithHelloWorld() throws Exception {
    Assertions.assertNotNull(client);
    Assertions.assertNotNull(list);
    System.out.println("Forwarding port");
    try (LocalPortForward p = client.pods().withName(pod.getMetadata().getName()).portForward(8080)) {
      assertTrue(p.isAlive());
      URL url = new URL("http://localhost:"+p.getLocalPort()+"/");

      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder().uri(url.toURI()).GET().build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      assertEquals(response.body(), "Hello world");
    } catch (Exception e)  {
      e.printStackTrace();
    }
  }
}
