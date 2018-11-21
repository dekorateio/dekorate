package io.ap4k.examples.s2i;

import io.ap4k.deps.kubernetes.client.DefaultKubernetesClient;
import io.ap4k.deps.kubernetes.client.KubernetesClient;
import io.ap4k.testing.openshift.SourceToImageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SourceToImageExtension.class)
public class SourceToImageExampleIT {

  @Test
  public void shouldRun() {
    KubernetesClient client = new DefaultKubernetesClient();
    client.pods().list().getItems().stream().forEach(p -> System.out.println(p.getMetadata().getName()));
  }
}
