package io.dekorate.testing;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;

public class DeploymentDiagnosticsFactory extends DiagnosticsFactory<Deployment> {
  @Override
  public DiagnosticsService<Deployment> create(KubernetesClient client) {
    return new DeploymentDiagnostics(client);
  }
}
