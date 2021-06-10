package io.dekorate.testing.openshift;

import io.dekorate.testing.DiagnosticsFactory;
import io.dekorate.testing.DiagnosticsService;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.api.model.DeploymentConfig;

public class DeploymentConfigDiagnosticsFactory extends DiagnosticsFactory<DeploymentConfig> {

  @Override
  public DiagnosticsService<DeploymentConfig> create(KubernetesClient client) {
    return new DeploymentConfigDiagnostics(client);
  }
}
