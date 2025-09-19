/**
 * Copyright 2019 The original authors.
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

import java.io.IOException;
import java.net.URL;

import io.dekorate.testing.annotation.Inject;
import io.dekorate.testing.annotation.Named;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.LocalPortForward;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//@KubernetesIntegrationTest
class ThorntailOnKubernetesIT {
  @Inject
  private KubernetesClient client;

  @Inject
  private KubernetesList list;

  @Inject
  @Named("thorntail-on-kubernetes-example")
  private Pod pod;

  // @Test
  void shouldRespondWithHelloWorld() throws IOException {
    assertNotNull(client);
    assertNotNull(list);
    try (LocalPortForward p = client.pods().withName(pod.getMetadata().getName()).portForward(9090)) { //port matches what is configured in project-defaults.yml
      assertTrue(p.isAlive());

      URL url = new URL("http://localhost:" + p.getLocalPort() + "/");
      OkHttpClient client = new OkHttpClient();
      Request request = new Request.Builder().get().url(url).build();
      Response response = client.newCall(request).execute();
      assertEquals("Hello world", response.body().string());
    }
  }
}
