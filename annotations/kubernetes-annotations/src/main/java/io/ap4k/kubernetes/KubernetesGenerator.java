package io.ap4k.kubernetes;

import io.ap4k.AbstractKubernetesGenerator;
import io.ap4k.Resources;
import io.ap4k.config.KubernetesConfig;
import io.ap4k.config.EditableKubernetesConfig;
import io.ap4k.config.Configuration;

public class KubernetesGenerator extends AbstractKubernetesGenerator<KubernetesConfig> {

  private static final String KUBERNETES = "kubernetes";

  public KubernetesGenerator () {
    this(new Resources());
  }

  public KubernetesGenerator(Resources resources) {
    super(resources);
  }

  public void generate(KubernetesConfig config) {
    resources.add(KUBERNETES, KubernetesResources.createDeployment(config));
    addVisitors(KUBERNETES, config);
  }

  public boolean accepts(Class<? extends Configuration> type) {
    return type.equals(KubernetesConfig.class) ||
      type.equals(EditableKubernetesConfig.class);
  }
}
