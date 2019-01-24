package io.ap4k.example.sbonkubernetes;

import io.ap4k.deps.kubernetes.api.model.Pod;
import io.ap4k.deps.kubernetes.client.KubernetesClient;
import io.ap4k.deps.kubernetes.client.LocalPortForward;
import io.ap4k.deps.okhttp3.OkHttpClient;
import io.ap4k.deps.okhttp3.Request;
import io.ap4k.deps.okhttp3.Response;
import io.ap4k.testing.annotation.Inject;
import io.ap4k.testing.annotation.KubernetesIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@KubernetesIntegrationTest
public class SpringBootOnKubernetesTests {

  @Inject
  private KubernetesClient client;

  @Inject
  Pod pod;

  @Test
  void shouldRespondWithHelloWorld() throws Exception {
    Assertions.assertNotNull(client);
    System.out.println("Forwarding port");
    LocalPortForward p = client.pods().withName(pod.getMetadata().getName()).portForward(8080);
    try {
      assertTrue(p.isAlive());
      URL url = new URL("http://localhost:"+p.getLocalPort()+"/");
      OkHttpClient client = new OkHttpClient();
      Request request = new Request.Builder().get().url(url).build();
      Response response = client.newCall(request).execute();
      assertEquals(response.body().string(), "Hello world");
    } finally {
      p.close();
    }
  }
}
