package io.dekorate.kind.manifest;

import java.util.Arrays;

import io.dekorate.ConfigurationRegistry;
import io.dekorate.ResourceRegistry;
import io.dekorate.kind.config.KindConfig;
import io.dekorate.kind.decorator.ApplyPortToKindServiceDecorator;
import io.dekorate.kind.decorator.ApplyServiceTypeToKindServiceDecorator;
import io.dekorate.kubernetes.config.KubernetesConfig;
import io.dekorate.kubernetes.decorator.AddServiceResourceDecorator;
import io.dekorate.kubernetes.decorator.ApplyImagePullPolicyDecorator;
import io.dekorate.kubernetes.manifest.KubernetesManifestGenerator;

public class KindManifestGenerator extends KubernetesManifestGenerator {

  private static final String KIND = "kind";

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
  protected void addDecorators(String group, KubernetesConfig config) {
    super.addDecorators(group, config);
    if (config.getPorts().length > 0) {
      resourceRegistry.decorate(group, new AddServiceResourceDecorator(config));
    }
  }

  @Override
  public void generate(KubernetesConfig kubernetesConfig) {
    initializeRegistry(kubernetesConfig);
    addDecorators(KIND, kubernetesConfig);

    configurationRegistry.get(KindConfig.class).ifPresent(c -> {
      resourceRegistry.decorate(KIND, new ApplyImagePullPolicyDecorator(kubernetesConfig.getName(), c.getImagePullPolicy()));
      resourceRegistry.decorate(KIND, new ApplyServiceTypeToKindServiceDecorator(kubernetesConfig.getName(), c));
      // Check if MinikubeConfig defines port, else fallback to KubernetesConfig
      if (c.getPorts().length > 0) {
        Arrays.stream(c.getPorts()).forEach(p -> {
          resourceRegistry.decorate(KIND, new ApplyPortToKindServiceDecorator(kubernetesConfig.getName(), p));
        });
      } else {
        Arrays.stream(kubernetesConfig.getPorts()).forEach(p -> {
          resourceRegistry.decorate(KIND, new ApplyPortToKindServiceDecorator(kubernetesConfig.getName(), p));
        });
      }
    });
  }
}
