package io.dekorate.minikube.manifest;

import java.util.HashMap;
import java.util.Optional;

import io.dekorate.AbstractKubernetesManifestGenerator;
import io.dekorate.BuildServiceFactories;
import io.dekorate.ConfigurationRegistry;
import io.dekorate.ResourceRegistry;
import io.dekorate.WithProject;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.kubernetes.config.ImageConfigurationBuilder;
import io.dekorate.kubernetes.config.KubernetesConfig;
import io.dekorate.kubernetes.decorator.AddServiceResourceDecorator;
import io.dekorate.minikube.config.EditableMinikubeConfig;
import io.dekorate.minikube.config.MinikubeConfig;
import io.dekorate.project.Project;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;

public class MinikubeManifestGenerator extends AbstractKubernetesManifestGenerator<MinikubeConfig> implements WithProject {

  private static final String MINIKUBE = "minikube";

  private final ConfigurationRegistry configurationRegistry;

  public MinikubeManifestGenerator() {
    this(new ResourceRegistry(), new ConfigurationRegistry());
  }

  public MinikubeManifestGenerator(ResourceRegistry resourceRegistry, ConfigurationRegistry configurationRegistry) {
    super(resourceRegistry);
    this.configurationRegistry = configurationRegistry;
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
  protected void addDecorators(String group, MinikubeConfig config) {
    super.addDecorators(group, config);
    if (config.getPorts().length > 0) {
      resourceRegistry.decorate(group, new AddServiceResourceDecorator(config));
    }
  }

  @Override
  public void generate(MinikubeConfig config) {
    ImageConfiguration imageConfig = getImageConfiguration(getProject(), config, configurationRegistry);

    Optional<Deployment> existingDeployment = resourceRegistry.groups().getOrDefault(MINIKUBE, new KubernetesListBuilder())
        .buildItems().stream()
        .filter(i -> i instanceof Deployment)
        .map(i -> (Deployment) i)
        .filter(i -> i.getMetadata().getName().equals(config.getName()))
        .findAny();

    if (!existingDeployment.isPresent()) {
      resourceRegistry.add(MINIKUBE, createDeployment(config, imageConfig));
    }

    addDecorators(MINIKUBE, config);
  }

  /**
   * Creates a {@link Deployment} for the {@link KubernetesConfig}.
   *
   * @param appConfig The session.
   * @return The deployment.
   */
  public Deployment createDeployment(MinikubeConfig appConfig, ImageConfiguration imageConfig) {
    return new DeploymentBuilder()
        .withNewMetadata()
        .withName(appConfig.getName())
        .endMetadata()
        .withNewSpec()
        .withReplicas(appConfig.getReplicas())
        .withNewSelector() //We need to have at least an empty selector so that the decorator can work with it.
        .withMatchLabels(new HashMap<String, String>())
        .endSelector()
        .withTemplate(createPodTemplateSpec(appConfig, imageConfig))
        .endSpec()
        .build();
  }

  public static PodTemplateSpec createPodTemplateSpec(MinikubeConfig appConfig, ImageConfiguration imageConfig) {
    return new PodTemplateSpecBuilder()
        .withSpec(createPodSpec(appConfig, imageConfig))
        .withNewMetadata()
        .endMetadata()
        .build();
  }

  public static PodSpec createPodSpec(MinikubeConfig appConfig, ImageConfiguration imageConfig) {
    return new PodSpecBuilder()
        .build();
  }

  private static ImageConfiguration getImageConfiguration(Project project, MinikubeConfig appConfig,
      ConfigurationRegistry configurationRegistry) {
    return configurationRegistry.getImageConfig(BuildServiceFactories.supplierMatches(project)).map(i -> merge(appConfig, i))
        .orElse(ImageConfiguration.from(appConfig));
  }

  private static ImageConfiguration merge(MinikubeConfig appConfig, ImageConfiguration imageConfig) {
    if (appConfig == null) {
      throw new NullPointerException("KubernetesConfig is null.");
    }
    if (imageConfig == null) {
      return ImageConfiguration.from(appConfig);
    }
    return new ImageConfigurationBuilder()
        .withProject(imageConfig.getProject() != null ? imageConfig.getProject() : appConfig.getProject())
        .withImage(imageConfig.getImage() != null ? imageConfig.getImage() : null)
        .withGroup(imageConfig.getGroup() != null ? imageConfig.getGroup() : null)
        .withName(imageConfig.getName() != null ? imageConfig.getName() : appConfig.getName())
        .withVersion(imageConfig.getVersion() != null ? imageConfig.getVersion() : appConfig.getVersion())
        .withRegistry(imageConfig.getRegistry() != null ? imageConfig.getRegistry() : null)
        .withDockerFile(imageConfig.getDockerFile() != null ? imageConfig.getDockerFile() : "Dockerfile")
        .withAutoBuildEnabled(imageConfig.isAutoBuildEnabled() ? imageConfig.isAutoBuildEnabled() : false)
        .withAutoPushEnabled(imageConfig.isAutoPushEnabled() ? imageConfig.isAutoPushEnabled() : false)
        .build();
  }


  @Override
  public boolean accepts(Class<? extends Configuration> config) {
    return config.equals(MinikubeConfig.class) || config.equals(EditableMinikubeConfig.class);
  }
}
