package io.dekorate.testing.openshift;

import io.dekorate.testing.DiagnosticsFactory;
import io.dekorate.testing.DiagnosticsService;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.api.model.ImageStream;

public class ImageStreamDiagnosticsFactory extends DiagnosticsFactory<ImageStream> {

  @Override
  public DiagnosticsService<ImageStream> create(KubernetesClient client) {
    return new ImageStreamDiagnostics(client);
  }
}
