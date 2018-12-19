package io.ap4k.example.sbonopenshift;

import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.api.model.Pod;
import io.ap4k.deps.kubernetes.client.KubernetesClient;
import io.ap4k.deps.kubernetes.client.LocalPortForward;
import io.ap4k.deps.okhttp3.OkHttpClient;
import io.ap4k.deps.okhttp3.Request;
import io.ap4k.deps.okhttp3.Response;
import io.ap4k.testing.annotation.Inject;
import io.ap4k.testing.openshift.annotation.OpenshiftIntegrationTest;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
      URL url = new URL("http://localhost:"+p.getLocalPort()+"/");

      OkHttpClient client = new OkHttpClient();
      Request request = new Request.Builder().get().url(url).build();
      Response response = client.newCall(request).execute();
      assertEquals(response.body().string(), "Hello world");
    } catch (Exception e)  {
      e.printStackTrace();
    }
  }

}
