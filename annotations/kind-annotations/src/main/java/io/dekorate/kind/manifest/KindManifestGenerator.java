package io.dekorate.kind.manifest;

import java.util.Optional;

import io.dekorate.ConfigurationRegistry;
import io.dekorate.ResourceRegistry;
import io.dekorate.kind.config.KindConfig;
import io.dekorate.kind.decorator.ApplyServiceTypeToKindServiceDecorator;
import io.dekorate.kubernetes.config.KubernetesConfig;
import io.dekorate.kubernetes.config.KubernetesConfigBuilder;
import io.dekorate.kubernetes.decorator.ApplyNodePortToServiceDecorator;
import io.dekorate.kubernetes.manifest.KubernetesManifestGenerator;

public class KindManifestGenerator extends KubernetesManifestGenerator {

  private static final String KIND = "kind";
  private static final String FALLBACK_TARGET_PORT = "http";

  public KindManifestGenerator() {
    this(new ResourceRegistry(), new ConfigurationRegistry());
  }

  public KindManifestGenerator(ResourceRegistry resourceRegistry, ConfigurationRegistry configurationRegistry) {
    super(resourceRegistry, configurationRegistry);
  }

  @Override
  public int order() {
    return 400;
  }

  @Override
  public String getKey() {
    return KIND;
  }

  @Override
  public void generate(KubernetesConfig kubernetesConfig) {
    initializeRegistry(kubernetesConfig);
    Optional<KindConfig> kindConfig = configurationRegistry.get(KindConfig.class);

    super.addDecorators(KIND, new KubernetesConfigBuilder(kubernetesConfig)
        .withImagePullPolicy(kindConfig.map(c -> c.getImagePullPolicy()).orElse(kubernetesConfig.getImagePullPolicy()))
        .build());

    kindConfig.ifPresent(c -> {
      resourceRegistry.decorate(KIND, new ApplyServiceTypeToKindServiceDecorator(kubernetesConfig.getName(), c));
      // Check if KindConfig defines port, else fallback to KubernetesConfig
      if (c.getPorts().length > 0) {
        resourceRegistry.decorate(KIND,
            new ApplyNodePortToServiceDecorator(kubernetesConfig, c.getPorts(), FALLBACK_TARGET_PORT));
      } else {
        resourceRegistry.decorate(KIND,
            new ApplyNodePortToServiceDecorator(kubernetesConfig, kubernetesConfig.getPorts(), FALLBACK_TARGET_PORT));
      }
    });
  }
}
