/**
 * Copyright 2018 The original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.openshift.manifest;

import java.util.Optional;
import java.util.stream.Collectors;

import io.dekorate.AbstractKubernetesManifestGenerator;
import io.dekorate.BuildServiceFactories;
import io.dekorate.ConfigurationRegistry;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.ResourceRegistry;
import io.dekorate.WithProject;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.kubernetes.config.ConfigKey;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.Container;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.kubernetes.config.ImageConfigurationBuilder;
import io.dekorate.kubernetes.config.Label;
import io.dekorate.kubernetes.configurator.ApplyDeployToApplicationConfiguration;
import io.dekorate.kubernetes.decorator.AddCommitIdAnnotationDecorator;
import io.dekorate.kubernetes.decorator.AddInitContainerDecorator;
import io.dekorate.kubernetes.decorator.AddLabelDecorator;
import io.dekorate.kubernetes.decorator.AddServiceResourceDecorator;
import io.dekorate.kubernetes.decorator.AddVcsUrlAnnotationDecorator;
import io.dekorate.kubernetes.decorator.ApplyHeadlessDecorator;
import io.dekorate.kubernetes.decorator.RemoveAnnotationDecorator;
import io.dekorate.openshift.OpenshiftAnnotations;
import io.dekorate.openshift.OpenshiftLabels;
import io.dekorate.openshift.config.EditableOpenshiftConfig;
import io.dekorate.openshift.config.OpenshiftConfig;
import io.dekorate.openshift.config.OpenshiftConfigBuilder;
import io.dekorate.openshift.decorator.AddRouteDecorator;
import io.dekorate.openshift.decorator.ApplyDeploymentTriggerDecorator;
import io.dekorate.openshift.decorator.ApplyReplicasDecorator;
import io.dekorate.option.config.VcsConfig;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.project.Project;
import io.dekorate.utils.Annotations;
import io.dekorate.utils.Git;
import io.dekorate.utils.Images;
import io.dekorate.utils.Labels;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;

public class OpenshiftManifestGenerator extends AbstractKubernetesManifestGenerator<OpenshiftConfig> implements WithProject {

  private static final String OPENSHIFT = "openshift";
  private static final String DEFAULT_REGISTRY = "docker.io";

  private static final String IF_NOT_PRESENT = "IfNotPresent";
  private static final String KUBERNETES_NAMESPACE = "KUBERNETES_NAMESPACE";
  private static final String METADATA_NAMESPACE = "metadata.namespace";

  public static final ConfigKey<String> RUNTIME_TYPE = new ConfigKey<>("RUNTIME_TYPE", String.class);

  private final Logger LOGGER = LoggerFactory.getLogger();
  private final ConfigurationRegistry configurationRegistry;

  public OpenshiftManifestGenerator(ResourceRegistry resourceRegistry, ConfigurationRegistry configurationRegistry) {
    super(resourceRegistry);
    this.configurationRegistry = configurationRegistry;
    resourceRegistry.groups().putIfAbsent(OPENSHIFT, new KubernetesListBuilder());
  }

  @Override
  public int order() {
    return 300;
  }

  @Override
  public String getKey() {
    return OPENSHIFT;
  }

  public void generate(OpenshiftConfig config) {
    LOGGER.info("Processing openshift configuration.");
    ImageConfiguration imageConfig = getImageConfiguration(getProject(), config, configurationRegistry);
    Optional<DeploymentConfig> existingDeploymentConfig = resourceRegistry.groups()
        .getOrDefault(OPENSHIFT, new KubernetesListBuilder()).buildItems().stream()
        .filter(i -> i instanceof DeploymentConfig)
        .map(i -> (DeploymentConfig) i)
        .filter(i -> i.getMetadata().getName().equals(config.getName()))
        .findAny();

    if (!existingDeploymentConfig.isPresent()) {
      resourceRegistry.add(OPENSHIFT, createDeploymentConfig(config, imageConfig));
    }

    if (config.isHeadless()) {
      resourceRegistry.decorate(OPENSHIFT, new ApplyHeadlessDecorator(config.getName()));
    }

    for (Container container : config.getInitContainers()) {
      resourceRegistry.decorate(OPENSHIFT, new AddInitContainerDecorator(config.getName(), container));
    }

    if (config.getPorts().length > 0) {
      resourceRegistry.decorate(OPENSHIFT, new AddServiceResourceDecorator(config));
    }

    addDecorators(OPENSHIFT, config, imageConfig);
  }

  @Override
  public ConfigurationSupplier<OpenshiftConfig> getFallbackConfig() {
    Project p = getProject();
    return new ConfigurationSupplier<OpenshiftConfig>(new OpenshiftConfigBuilder()
        .accept(new ApplyDeployToApplicationConfiguration()).accept(new ApplyProjectInfo(p)));
  }

  protected void addDecorators(String group, OpenshiftConfig config, ImageConfiguration imageConfig) {
    super.addDecorators(group, config);
    if (config.getReplicas() != 1) {
      resourceRegistry.decorate(group, new ApplyReplicasDecorator(config.getName(), config.getReplicas()));
    }
    resourceRegistry.decorate(group,
        new ApplyDeploymentTriggerDecorator(config.getName(), imageConfig.getName() + ":" + imageConfig.getVersion()));
    resourceRegistry.decorate(group, new AddRouteDecorator(config));

    if (config.hasAttribute(RUNTIME_TYPE)) {
      resourceRegistry.decorate(group, new AddLabelDecorator(config.getName(),
          new Label(OpenshiftLabels.RUNTIME, config.getAttribute(RUNTIME_TYPE), new String[0])));
    }
    resourceRegistry.decorate(group, new RemoveAnnotationDecorator(config.getName(), Annotations.VCS_URL));

    Project project = getProject();
    Optional<VcsConfig> vcsConfig = configurationRegistry.get(VcsConfig.class);
    String remote = vcsConfig.map(VcsConfig::getRemote).orElse(Git.ORIGIN);
    boolean httpsPrefered = vcsConfig.map(VcsConfig::isHttpsPreferred).orElse(false);

    String vcsUrl = project.getScmInfo() != null && Strings.isNotNullOrEmpty(project.getScmInfo().getRemote().get(Git.ORIGIN))
        ? Git.getRemoteUrl(project.getRoot(), remote, httpsPrefered).orElse(Labels.UNKNOWN)
        : Labels.UNKNOWN;

    resourceRegistry.decorate(group, new AddVcsUrlAnnotationDecorator(config.getName(), OpenshiftAnnotations.VCS_URL, vcsUrl));
    resourceRegistry.decorate(group, new AddCommitIdAnnotationDecorator());
  }

  public boolean accepts(Class<? extends Configuration> type) {
    return type.equals(OpenshiftConfig.class) ||
        type.equals(EditableOpenshiftConfig.class);
  }

  /**
   * Creates a {@link DeploymentConfig} for the {@link OpenshiftConfig}.
   * 
   * @param config The sesssion.
   * @return The deployment config.
   */
  public DeploymentConfig createDeploymentConfig(OpenshiftConfig config, ImageConfiguration imageConfig) {
    return new DeploymentConfigBuilder()
        .withNewMetadata()
        .withName(config.getName())
        // We are adding the labels up front as they might be picked up by auxilliary resources
        .withLabels(Labels.createLabels(config).stream().collect(Collectors.toMap(l -> l.getKey(), l -> l.getValue())))
        .endMetadata()
        .withNewSpec()
        .withReplicas(1)
        .withTemplate(createPodTemplateSpec(config, imageConfig))
        .endSpec()
        .build();
  }

  /**
   * Creates a {@link PodTemplateSpec} for the {@link OpenshiftConfig}.
   * 
   * @param config The sesssion.
   * @return The pod template specification.
   */
  public PodTemplateSpec createPodTemplateSpec(OpenshiftConfig config, ImageConfiguration imageConfig) {
    return new PodTemplateSpecBuilder()
        .withSpec(createPodSpec(config, imageConfig))
        .withNewMetadata()
        .endMetadata()
        .build();
  }

  /**
   * Creates a {@link PodSpec} for the {@link OpenshiftConfig}.
   * 
   * @param config The sesssion.
   * @return The pod specification.
   */
  public static PodSpec createPodSpec(OpenshiftConfig config, ImageConfiguration imageConfig) {
    String image = Images.getImage(imageConfig.getRegistry(), imageConfig.getGroup(), imageConfig.getName(),
        imageConfig.getVersion());

    return new PodSpecBuilder()
        .addNewContainer()
        .withName(config.getName())
        .withImage(image)
        .withImagePullPolicy(IF_NOT_PRESENT)
        .addNewEnv()
        .withName(KUBERNETES_NAMESPACE)
        .withNewValueFrom()
        .withNewFieldRef(null, METADATA_NAMESPACE)
        .endValueFrom()
        .endEnv()
        .endContainer()
        .build();
  }

  private static ImageConfiguration getImageConfiguration(Project project, OpenshiftConfig config,
      ConfigurationRegistry configurationRegistry) {
    return configurationRegistry.getImageConfig(BuildServiceFactories.supplierMatches(project))
        .map(i -> merge(config, i))
        .orElse(ImageConfiguration.from(config));
  }

  private static ImageConfiguration merge(OpenshiftConfig config, ImageConfiguration imageConfig) {
    if (config == null) {
      throw new NullPointerException("OpenshiftConfig is null.");
    }
    if (imageConfig == null) {
      return ImageConfiguration.from(config);
    }
    return new ImageConfigurationBuilder()
        .withProject(imageConfig.getProject() != null ? imageConfig.getProject() : config.getProject())
        .withImage(imageConfig.getImage() != null ? imageConfig.getImage() : null)
        .withGroup(imageConfig.getGroup() != null ? imageConfig.getGroup() : null)
        .withRegistry(imageConfig.getRegistry() != null ? imageConfig.getRegistry() : DEFAULT_REGISTRY)
        .withName(imageConfig.getName() != null ? imageConfig.getName() : config.getName())
        .withVersion(imageConfig.getVersion() != null ? imageConfig.getVersion() : config.getVersion())
        .withAutoBuildEnabled(imageConfig.isAutoBuildEnabled() ? imageConfig.isAutoBuildEnabled() : false)
        .withAutoPushEnabled(imageConfig.isAutoPushEnabled() ? imageConfig.isAutoPushEnabled() : false)
        .build();
  }
}
