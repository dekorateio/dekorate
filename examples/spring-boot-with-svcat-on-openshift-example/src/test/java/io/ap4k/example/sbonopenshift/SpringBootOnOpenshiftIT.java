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
package io.ap4k.example.sbonopenshift;

import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.api.model.Pod;
import io.ap4k.deps.kubernetes.client.KubernetesClient;
import io.ap4k.deps.kubernetes.client.LocalPortForward;
import io.ap4k.deps.okhttp3.*;
import io.ap4k.testing.annotation.Inject;
import io.ap4k.testing.annotation.OnServicePresentCondition;
import io.ap4k.testing.openshift.annotation.OpenshiftIntegrationTest;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@OnServicePresentCondition(value = "apiserver", namespace = "kube-service-catalog")
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
      final String firstName = "John";
      final String lastName = "Doe";
      final MediaType json = MediaType.parse("application/json");
      Request request = new Request.Builder().post(RequestBody.create(json, "{ \"firsName\" : \""
        + firstName + "\", \"lastName\" : \"" + lastName + "\"}")).url(url).build();
      Response response = client.newCall(request).execute();

     assertTrue(response.isSuccessful());

      final Response person = client.newCall(new Request.Builder().get().url(url.toExternalForm() + "/" + lastName).build()).execute();
      assertTrue(person.isSuccessful());
      final ResponseBody body = person.body();
      assertNotNull(body);
      assertEquals(body.contentType(), json);
      assertTrue(body.string().contains(lastName));
      assertTrue(body.string().contains(firstName));

    } catch (Exception e)  {
      e.printStackTrace();
    }
  }

}
