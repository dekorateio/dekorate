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
package io.dekorate.example.sbonopenshift

import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.LocalPortForward
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import io.dekorate.testing.annotation.Inject
import io.dekorate.testing.annotation.Named
import io.dekorate.testing.openshift.annotation.OpenshiftIntegrationTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

@OpenshiftIntegrationTest
class SpringBootOnOpenshiftIT {

  @Inject
  private KubernetesClient client

  @Inject
  @Named("spring-boot-with-groovy-on-openshift-example")
  Pod pod

  @Test
  void shouldRespondWithHelloWorld() throws Exception {
    Assertions.assertNotNull(client)
    System.out.println("Forwarding port")
    LocalPortForward p = client.pods().withName(pod.getMetadata().getName()).portForward(8080)
    try {
      assertTrue(p.isAlive())
      URL url = new URL("http://localhost:"+p.getLocalPort()+"/")
      OkHttpClient client = new OkHttpClient()
      Request request = new Request.Builder().get().url(url).build()
      Response response = client.newCall(request).execute()
      assertEquals(response.body().string(), "Hello world")
    } finally {
      p.close()
    }
  }
}
