package io.dekorate.testing;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;

public class PodDiagnosticsFactory extends DiagnosticsFactory<Pod> {
  @Override
  public DiagnosticsService<Pod> create(KubernetesClient client) {
    return new PodDiagnostics(client);
  }
}
