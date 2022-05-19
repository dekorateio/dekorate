package io.dekorate.example;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import io.dekorate.testing.annotation.Inject;
import io.fabric8.kubernetes.client.KubernetesClient;

public abstract class BaseIT {

  @Inject
  private KubernetesClient kubernetesClient;

  @Test
  public void shouldClientBeInjected() throws Exception {
    assertNotNull(kubernetesClient, "Kubernetes Client is not injected!");
  }
}
