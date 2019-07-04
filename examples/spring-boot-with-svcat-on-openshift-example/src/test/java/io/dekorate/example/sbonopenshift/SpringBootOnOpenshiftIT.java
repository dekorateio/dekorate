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
package io.dekorate.example.sbonopenshift;

import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.deps.kubernetes.api.model.Pod;
import io.dekorate.deps.kubernetes.client.KubernetesClient;
import io.dekorate.deps.kubernetes.client.LocalPortForward;
import io.dekorate.deps.okhttp3.MediaType;
import io.dekorate.deps.okhttp3.OkHttpClient;
import io.dekorate.deps.okhttp3.Request;
import io.dekorate.deps.okhttp3.RequestBody;
import io.dekorate.deps.okhttp3.Response;
import io.dekorate.testing.annotation.Inject;
import io.dekorate.testing.annotation.OnServicePresentCondition;
import io.dekorate.testing.openshift.annotation.OpenshiftIntegrationTest;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@OnServicePresentCondition(value = "api-server", namespace = "kube-service-catalog")
@OpenshiftIntegrationTest
class SpringBootOnOpenshiftIT {
  @Inject
  private KubernetesClient client;

  @Inject
  private KubernetesList list;

  @Inject
  Pod pod;

  @Test
  public void shouldRespondWithHelloWorld() throws Exception {
    assertNotNull(client);
    assertNotNull(list);
    System.out.println("Forwarding port");
    try (LocalPortForward p = client.pods().withName(pod.getMetadata().getName()).portForward(8080)) {
      assertTrue(p.isAlive());
      URL url = new URL("http://localhost:"+p.getLocalPort()+"/people");

      OkHttpClient client = new OkHttpClient();
      Request request = new Request.Builder().post(RequestBody.create(MediaType.parse("application/json"), "{ \"firsName\" : \"John\", \"lastName\" : \"Doe\"}")).url(url).build();
      Response response = client.newCall(request).execute();

     assertTrue(response.isSuccessful());
    } catch (Exception e)  {
      e.printStackTrace();
    }
  }

}
