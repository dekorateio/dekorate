package io.ap4k.kubernetes;

import io.ap4k.AbstractKubernetesGenerator;
import io.ap4k.Resources;
import io.ap4k.config.KubernetesConfig;

public class KubernetesGenerator extends AbstractKubernetesGenerator<KubernetesConfig> {

    private static final String KUBERNETES = "kubernetes";

    public KubernetesGenerator(Resources resources) {
        super(resources);
    }

    public void generate(KubernetesConfig config) {
        resources.add(KUBERNETES, KubernetesResources.createDeployment(config));
        addVisitors(config);
    }

  @Override
  public Class<? extends KubernetesConfig> getType() {
    return KubernetesConfig.class;
  }
}
