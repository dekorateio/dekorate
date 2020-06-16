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
package io.dekorate.openshift.handler;

import java.util.Map;
import java.util.Optional;

import io.dekorate.AbstractKubernetesHandler;
import io.dekorate.BuildServiceFactories;
import io.dekorate.Configurators;
import io.dekorate.Handler;
import io.dekorate.HandlerFactory;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.Resources;
import io.dekorate.WithProject;
import io.dekorate.config.ConfigurationSupplier;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import io.dekorate.kubernetes.config.Annotation;
import io.dekorate.kubernetes.config.ConfigKey;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.Container;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.kubernetes.config.ImageConfigurationBuilder;
import io.dekorate.kubernetes.config.Label;
import io.dekorate.kubernetes.configurator.ApplyDeployToApplicationConfiguration;
import io.dekorate.kubernetes.decorator.AddAnnotationDecorator;
import io.dekorate.kubernetes.decorator.AddInitContainerDecorator;
import io.dekorate.kubernetes.decorator.AddLabelDecorator;
import io.dekorate.kubernetes.decorator.AddServiceResourceDecorator;
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
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.project.Project;
import io.dekorate.utils.Annotations;
import io.dekorate.utils.Labels;
import io.dekorate.utils.Strings;

public class OpenshiftHandler extends AbstractKubernetesHandler<OpenshiftConfig> implements HandlerFactory, WithProject {

  private static final String OPENSHIFT = "openshift";

  private static final String IF_NOT_PRESENT = "IfNotPresent";
  private static final String KUBERNETES_NAMESPACE = "KUBERNETES_NAMESPACE";
  private static final String METADATA_NAMESPACE = "metadata.namespace";

  private static final String IMAGESTREAMTAG = "ImageStreamTag";
  private static final String IMAGECHANGE = "ImageChange";

  private static final String JAVA_APP_JAR = "JAVA_APP_JAR";

  public static final ConfigKey<String> RUNTIME_TYPE = new ConfigKey<>("RUNTIME_TYPE", String.class);

  private final Logger LOGGER = LoggerFactory.getLogger();
  private final Configurators configurators;

  public OpenshiftHandler() {
    this(new Resources(), new Configurators());
  }
  public OpenshiftHandler(Resources resources, Configurators configurators) {
    super(resources);
    this.configurators = configurators;
  }

  @Override
  public Handler create(Resources resources, Configurators configurators) {
    return new OpenshiftHandler(resources, configurators);
  }

  @Override
  public int order() {
    return 300;
  }

  @Override
  public String getKey() {
    return OPENSHIFT;
  }

  public void handle(OpenshiftConfig config) {
    LOGGER.info("Processing openshift configuration.");
    ImageConfiguration imageConfig = getImageConfiguration(getProject(), config, configurators);
    Optional<DeploymentConfig> existingDeploymentConfig = resources.groups().getOrDefault(OPENSHIFT, new KubernetesListBuilder()).buildItems().stream()
      .filter(i -> i instanceof DeploymentConfig)
      .map(i -> (DeploymentConfig)i)
      .filter(i -> i.getMetadata().getName().equals(config.getName()))
      .findAny();

    if (!existingDeploymentConfig.isPresent()) {
      resources.add(OPENSHIFT, createDeploymentConfig(config, imageConfig));
    }

    if (config.isHeadless()) {
      resources.decorate(OPENSHIFT, new ApplyHeadlessDecorator(config.getName()));
    }

    for (Container container : config.getInitContainers()) {
      resources.decorate(OPENSHIFT, new AddInitContainerDecorator(config.getName(), container));
    }

    if (config.getPorts().length > 0) {
      resources.decorate(OPENSHIFT, new AddServiceResourceDecorator(config));
    }

    addDecorators(OPENSHIFT, config, imageConfig);
  }

  @Override
  public ConfigurationSupplier<OpenshiftConfig> getFallbackConfig() {
    Project p = getProject();
    return new ConfigurationSupplier<OpenshiftConfig>(new OpenshiftConfigBuilder().accept(new ApplyDeployToApplicationConfiguration()).accept(new ApplyProjectInfo(p)));
  }


  protected void addDecorators(String group, OpenshiftConfig config, ImageConfiguration imageConfig) {
    super.addDecorators(group, config);
    if (config.getReplicas() != 1) {
      resources.decorate(group, new ApplyReplicasDecorator(config.getName(), config.getReplicas()));
    }
    resources.decorate(group, new ApplyDeploymentTriggerDecorator(config.getName(), imageConfig.getName() + ":" + imageConfig.getVersion()));
    resources.decorate(group, new AddRouteDecorator(config));

    if (config.hasAttribute(RUNTIME_TYPE)) {
      resources.decorate(group, new AddLabelDecorator(new Label(OpenshiftLabels.RUNTIME, config.getAttribute(RUNTIME_TYPE))));
    }
    resources.decorate(group, new RemoveAnnotationDecorator(Annotations.VCS_URL));
    Project p = getProject();
    String vcsUrl = p.getScmInfo() != null && Strings.isNotNullOrEmpty( p.getScmInfo().getUrl())
      ? p.getScmInfo().getUrl()
      : Labels.UNKNOWN;
    resources.decorate(group, new AddAnnotationDecorator(new Annotation(OpenshiftAnnotations.VCS_URL, vcsUrl)));
  }

  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(OpenshiftConfig.class) ||
      type.equals(EditableOpenshiftConfig.class);
  }

  /**
   * Creates a {@link DeploymentConfig} for the {@link OpenshiftConfig}.
   * @param config   The sesssion.
   * @return          The deployment config.
   */
  public DeploymentConfig createDeploymentConfig(OpenshiftConfig config, ImageConfiguration imageConfig)  {
    Map<String, String> labels = Labels.createLabels(config);
    return new DeploymentConfigBuilder()
      .withNewMetadata()
      .withName(config.getName())
      .withLabels(labels)
      .endMetadata()
      .withNewSpec()
      .withReplicas(1)
      .withTemplate(createPodTemplateSpec(config, labels))
      .withSelector(labels)
      .addNewTrigger()
      .withType(IMAGECHANGE)
      .withNewImageChangeParams()
      .withAutomatic(true)
      .withContainerNames(config.getName())
      .withNewFrom()
      .withKind(IMAGESTREAMTAG)
      .withName(imageConfig.getName() + ":" + imageConfig.getVersion())
      .endFrom()
      .endImageChangeParams()
      .endTrigger()
      .endSpec()
      .build();
  }

  /**
   * Creates a {@link PodTemplateSpec} for the {@link OpenshiftConfig}.
   * @param config   The sesssion.
   * @return          The pod template specification.
   */
  public PodTemplateSpec createPodTemplateSpec(OpenshiftConfig config, Map<String, String> labels) {
    return new PodTemplateSpecBuilder()
      .withSpec(createPodSpec(config))
      .withNewMetadata()
      .withLabels(labels)
      .endMetadata()
      .build();
  }

  /**
   * Creates a {@link PodSpec} for the {@link OpenshiftConfig}.
   * @param config   The sesssion.
   * @return          The pod specification.
   */
  public static PodSpec createPodSpec(OpenshiftConfig config) {
    return new PodSpecBuilder()
      .addNewContainer()
      .withName(config.getName())
      .withImage("")
      .withImagePullPolicy(IF_NOT_PRESENT)
      .addNewEnv()
      .withName(KUBERNETES_NAMESPACE)
      .withNewValueFrom()
      .withNewFieldRef(null, METADATA_NAMESPACE)
      .endValueFrom()
      .endEnv()
      .addNewEnv()
      .withName(JAVA_APP_JAR)
      .withValue("/deployments/" + config.getProject().getBuildInfo().getOutputFile().getFileName().toString())
      .endEnv()
      .endContainer()
      .build();
  }
 
  private static ImageConfiguration getImageConfiguration(Project project, OpenshiftConfig config, Configurators configurators) {
    return configurators.getImageConfig(BuildServiceFactories.supplierMatches(project)).map(i -> merge(config, i)).orElse(ImageConfiguration.from(config));
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
      .withGroup(imageConfig.getGroup() != null ? imageConfig.getGroup() : null)
      .withName(imageConfig.getName() != null ? imageConfig.getName() : config.getName())
      .withVersion(imageConfig.getVersion() != null ? imageConfig.getVersion() : config.getVersion())
      .withAutoBuildEnabled(imageConfig.isAutoBuildEnabled() ? imageConfig.isAutoBuildEnabled() : false)
      .withAutoPushEnabled(imageConfig.isAutoPushEnabled() ? imageConfig.isAutoPushEnabled() : false)
      .build();
  }
}
