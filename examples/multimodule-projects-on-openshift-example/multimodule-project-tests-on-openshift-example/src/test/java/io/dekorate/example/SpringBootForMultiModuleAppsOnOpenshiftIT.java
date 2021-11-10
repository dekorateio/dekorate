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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.dekorate.testing.annotation.Inject;
import io.dekorate.testing.annotation.Named;
import io.dekorate.testing.openshift.annotation.OpenshiftIntegrationTest;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.api.model.Route;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

 //TODO: Re-enable the test once CI issues are resolved.
@Disabled
@OpenshiftIntegrationTest(additionalModules = { "../multimodule-project-a-on-openshift-example", "../multimodule-project-b-on-openshift-example" })
class SpringBootForMultiModuleAppsOnOpenshiftIT {

  @Inject
  private KubernetesClient client;

  @Inject
  private KubernetesList allResources;

  @Inject
  @Named("multimodule-project-a-on-openshift-example")
  Pod podForProjectA;

  @Inject
  @Named("multimodule-project-a-on-openshift-example")
  private Route routeForProjectA;

  @Inject
  @Named("multimodule-project-a-on-openshift-example")
  private URL appUrlForProjectA;

  @Inject
  @Named("multimodule-project-b-on-openshift-example")
  Pod podForProjectB;

  @Inject
  @Named("multimodule-project-b-on-openshift-example")
  private Route routeForProjectB;

  @Inject
  @Named("multimodule-project-b-on-openshift-example")
  private URL appUrlForProjectB;

  @Test
  public void shouldInjectAllInstances() {
    assertNotNull(client);
    assertNotNull(allResources);

    // project a
    assertNotNull(podForProjectA);
    assertNotNull(routeForProjectA);
    assertNotNull(appUrlForProjectA);

    // project b
    assertNotNull(podForProjectB);
    assertNotNull(routeForProjectB);
    assertNotNull(appUrlForProjectB);
  }

  @Test
  public void shouldRespondWithHelloWorld() throws Exception {
    assertHelloWorld(appUrlForProjectA);
    assertHelloWorld(appUrlForProjectB);
  }

  private void assertHelloWorld(URL appUrl) throws IOException {
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().get().url(appUrl).build();
    Response response = client.newCall(request).execute();
    assertEquals(response.body().string(), "Hello world");
  }

}
