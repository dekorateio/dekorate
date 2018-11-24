package io.ap4k.kubernetes;

import io.ap4k.AbstractKubernetesProcessor;
import io.ap4k.Resources;
import io.ap4k.config.KubernetesConfig;
import io.ap4k.config.EditableKubernetesConfig;
import io.ap4k.config.Configuration;

public class KubernetesProcessor extends AbstractKubernetesProcessor<KubernetesConfig> {

  private static final String KUBERNETES = "kubernetes";

  public KubernetesProcessor() {
    this(new Resources());
  }

  public KubernetesProcessor(Resources resources) {
    super(resources);
  }

  public void process(KubernetesConfig config) {
    resources.add(KUBERNETES, KubernetesResources.createDeployment(config));
    addVisitors(KUBERNETES, config);
  }

  public boolean accepts(Class<? extends Configuration> type) {
    return type.equals(KubernetesConfig.class) ||
      type.equals(EditableKubernetesConfig.class);
  }
}
