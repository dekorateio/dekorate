package io.dekorate.minikube.manifest;

import java.util.Arrays;

import io.dekorate.ConfigurationRegistry;
import io.dekorate.ResourceRegistry;
import io.dekorate.kubernetes.config.KubernetesConfig;
import io.dekorate.kubernetes.decorator.AddServiceResourceDecorator;
import io.dekorate.kubernetes.manifest.KubernetesManifestGenerator;
import io.dekorate.minikube.config.MinikubeConfig;
import io.dekorate.minikube.decorator.ApplyPortToMinikubeServiceDecorator;
import io.dekorate.minikube.decorator.ApplyServiceTypeToMinikubeServiceDecorator;

public class MinikubeManifestGenerator extends KubernetesManifestGenerator {

  private static final String MINIKUBE = "minikube";

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
        Arrays.stream(c.getPorts()).forEach(p -> {
          resourceRegistry.decorate(MINIKUBE, new ApplyPortToMinikubeServiceDecorator(kubernetesConfig.getName(), p));
        });
      } else {
        Arrays.stream(kubernetesConfig.getPorts()).forEach(p -> {
          resourceRegistry.decorate(MINIKUBE, new ApplyPortToMinikubeServiceDecorator(kubernetesConfig.getName(), p));
        });
      }

    });
  }
}
