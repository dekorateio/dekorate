package io.dekorate.minikube.manifest;

import io.dekorate.ConfigurationRegistry;
import io.dekorate.ResourceRegistry;
import io.dekorate.kubernetes.config.KubernetesConfig;
import io.dekorate.kubernetes.decorator.AddServiceResourceDecorator;
import io.dekorate.kubernetes.decorator.ApplyNodePortToServiceDecorator;
import io.dekorate.kubernetes.manifest.KubernetesManifestGenerator;
import io.dekorate.minikube.config.MinikubeConfig;
import io.dekorate.minikube.decorator.ApplyServiceTypeToMinikubeServiceDecorator;

public class MinikubeManifestGenerator extends KubernetesManifestGenerator {

  private static final String MINIKUBE = "minikube";
  private static final String FALLBACK_TARGET_PORT = "http";

  public MinikubeManifestGenerator() {
    this(new ResourceRegistry(), new ConfigurationRegistry());
  }

  public MinikubeManifestGenerator(ResourceRegistry resourceRegistry, ConfigurationRegistry configurationRegistry) {
    super(resourceRegistry, configurationRegistry);
  }

  @Override
  public int order() {
    return 400;
  }

  @Override
  public String getKey() {
    return MINIKUBE;
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
    addDecorators(MINIKUBE, kubernetesConfig);

    configurationRegistry.get(MinikubeConfig.class).ifPresent(c -> {
      resourceRegistry.decorate(MINIKUBE, new ApplyServiceTypeToMinikubeServiceDecorator(kubernetesConfig.getName(), c));
      // Check if MinikubeConfig defines port, else fallback to KubernetesConfig
      if (c.getPorts().length > 0) {
        resourceRegistry.decorate(MINIKUBE,
            new ApplyNodePortToServiceDecorator(kubernetesConfig, c.getPorts(), FALLBACK_TARGET_PORT));
      } else {
        resourceRegistry.decorate(MINIKUBE,
            new ApplyNodePortToServiceDecorator(kubernetesConfig, kubernetesConfig.getPorts(), FALLBACK_TARGET_PORT));
      }

    });
  }
}
