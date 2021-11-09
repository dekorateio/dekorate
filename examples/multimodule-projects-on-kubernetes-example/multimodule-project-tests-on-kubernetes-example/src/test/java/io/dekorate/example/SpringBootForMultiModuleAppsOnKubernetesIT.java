/**
 * Copyright 2021 The original authors.
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

import io.dekorate.testing.annotation.KubernetesIntegrationTest;
import io.dekorate.testing.annotation.Named;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.LocalPortForward;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import io.dekorate.testing.annotation.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@KubernetesIntegrationTest(additionalModules = { "../multimodule-project-a-on-kubernetes-example", "../multimodule-project-b-on-kubernetes-example" })
class SpringBootForMultiModuleAppsOnKubernetesIT {

  @Inject
  private KubernetesClient client;

  @Inject
  private KubernetesList allResources;

  @Inject
  @Named("multimodule-project-a-on-kubernetes-example")
  Pod podForProjectA;

  @Inject
  @Named("multimodule-project-b-on-kubernetes-example")
  Pod podForProjectB;

  @Test
  public void shouldInjectAllInstances() {
    assertNotNull(client);
    assertNotNull(allResources);

    // project a
    assertNotNull(podForProjectA);

    // project b
    assertNotNull(podForProjectB);
  }

  @Test
  public void shouldRespondWithHelloWorld() throws Exception {
    assertHelloWorld(podForProjectA);
    assertHelloWorld(podForProjectB);
  }

  private void assertHelloWorld(Pod pod) throws IOException {
    try (LocalPortForward p = client.pods().withName(pod.getMetadata().getName()).portForward(9090)) {
      assertTrue(p.isAlive());
      URL url = new URL("http://localhost:"+p.getLocalPort()+"/");

      OkHttpClient client = new OkHttpClient();
      Request request = new Request.Builder().get().url(url).build();
      Response response = client.newCall(request).execute();
      assertEquals(response.body().string(), "Hello world");
    }
  }

}
