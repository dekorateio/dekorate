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
package io.dekorate.kubernetes.handler;

import io.dekorate.AbstractKubernetesHandler;
import io.dekorate.Configurators;
import io.dekorate.Handler;
import io.dekorate.HandlerFactory;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.Resources;
import io.dekorate.WithProject;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.project.Project;
import io.dekorate.utils.Strings;
import io.dekorate.deps.kubernetes.api.model.KubernetesListBuilder;
import io.dekorate.deps.kubernetes.api.model.LabelSelector;
import io.dekorate.deps.kubernetes.api.model.LabelSelectorBuilder;
import io.dekorate.deps.kubernetes.api.model.PodSpec;
import io.dekorate.deps.kubernetes.api.model.PodSpecBuilder;
import io.dekorate.deps.kubernetes.api.model.PodTemplateSpec;
import io.dekorate.deps.kubernetes.api.model.PodTemplateSpecBuilder;
import io.dekorate.deps.kubernetes.api.model.apps.Deployment;
import io.dekorate.deps.kubernetes.api.model.apps.DeploymentBuilder;
import io.dekorate.kubernetes.config.Container;
import io.dekorate.kubernetes.config.KubernetesConfig;
import io.dekorate.kubernetes.config.KubernetesConfigBuilder;
import io.dekorate.kubernetes.configurator.ApplyAutoBuild;
import io.dekorate.kubernetes.config.EditableKubernetesConfig;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.decorator.AddIngressDecorator;
import io.dekorate.kubernetes.decorator.AddInitContainerDecorator;
import io.dekorate.kubernetes.decorator.AddSidecarDecorator;
import io.dekorate.kubernetes.decorator.ApplyImageDecorator;
import io.dekorate.kubernetes.decorator.ApplyLabelSelectorDecorator;
import io.dekorate.project.ApplyProjectInfo;

import java.util.Optional;

import static io.dekorate.utils.Labels.createLabels;

public class KubernetesHandler extends AbstractKubernetesHandler<KubernetesConfig> implements HandlerFactory, WithProject {

  private static final String KUBERNETES = "kubernetes";
  private static final String DEFAULT_REGISTRY = "docker.io";

  private static final String IF_NOT_PRESENT = "IfNotPresent";
  private static final String KUBERNETES_NAMESPACE = "KUBERNETES_NAMESPACE";
  private static final String METADATA_NAMESPACE = "metadata.namespace";
  private final Logger LOGGER = LoggerFactory.getLogger();

  public KubernetesHandler() {
    this(new Resources());
  }

  public KubernetesHandler(Resources resources) {
    super(resources);
  }

  @Override
  public Handler create(Resources resources, Configurators configurators) {
    return new KubernetesHandler(resources);
  }

  @Override
  public int order() {
    return 200;
  }

  public void handle(KubernetesConfig config) {
    LOGGER.info("Processing kubernetes configuration.");
    setApplicationInfo(config);
    Optional<Deployment> existingDeployment = resources.groups().getOrDefault(KUBERNETES, new KubernetesListBuilder()).buildItems().stream()
      .filter(i -> i instanceof Deployment)
      .map(i -> (Deployment)i)
      .filter(i -> i.getMetadata().getName().equals(config.getName()))
      .findAny();

    if (!existingDeployment.isPresent()) {
      resources.add(KUBERNETES, createDeployment(config));
    }
    addDecorators(KUBERNETES, config);
  }

  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(KubernetesConfig.class) ||
      type.equals(EditableKubernetesConfig.class);
  }

  @Override
  protected void addDecorators(String group, KubernetesConfig config) {
    super.addDecorators(group, config);

    resources.decorate(group, new AddIngressDecorator(config, resources.getLabels()));
    resources.decorate(group, new ApplyLabelSelectorDecorator(createSelector()));
    if  (Strings.isNotNullOrEmpty(config.getRegistry()))  {
      resources.decorate(group, new ApplyImageDecorator(config.getName(), config.getRegistry() + "/" + config.getGroup() + "/" + config.getName() + ":" + config.getVersion()));
    } else if (config.isAutoPushEnabled()) {
      resources.decorate(group, new ApplyImageDecorator(config.getName(), DEFAULT_REGISTRY + "/" + config.getGroup() + "/" + config.getName() + ":" + config.getVersion()));
    } else {
      resources.decorate(group, new ApplyImageDecorator(config.getName(), config.getGroup() + "/" + config.getName() + ":" + config.getVersion()));
    }
  }

  /**
   * Creates a {@link Deployment} for the {@link KubernetesConfig}.
   * @param config   The session.
   * @return          The deployment.
   */
  public Deployment createDeployment(KubernetesConfig config)  {
    return new DeploymentBuilder()
      .withNewMetadata()
      .withName(config.getName())
      .withLabels(resources.getLabels())
      .endMetadata()
      .withNewSpec()
      .withReplicas(1)
      .withTemplate(createPodTemplateSpec(config))
      .withSelector(createSelector())
      .endSpec()
      .build();
  }


  /**
   * Creates a {@link LabelSelector} that matches the labels for the {@link KubernetesConfig}.
   * @return          A labels selector.
   */
  public LabelSelector createSelector() {
    return new LabelSelectorBuilder()
      .withMatchLabels(resources.getLabels())
      .build();
  }


  /**
   * Creates a {@link PodTemplateSpec} for the {@link KubernetesConfig}.
   * @param config   The sesssion.
   * @return          The pod template specification.
   */
  public static PodTemplateSpec createPodTemplateSpec(KubernetesConfig config) {
    return new PodTemplateSpecBuilder()
      .withSpec(createPodSpec(config))
      .withNewMetadata()
      .withLabels(createLabels(config))
      .endMetadata()
      .build();
  }

  /**
   * Creates a {@link PodSpec} for the {@link KubernetesConfig}.
   * @param config   The sesssion.
   * @return          The pod specification.
   */
  public static PodSpec createPodSpec(KubernetesConfig config) {
    return new PodSpecBuilder()
      .addNewContainer()
      .withName(config.getName())
      .withImage(config.getGroup() + "/" + config.getName() + ":" + config.getVersion())
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

  @Override
  public ConfigurationSupplier<KubernetesConfig> getFallbackConfig() {
    Project p = getProject();
    return new ConfigurationSupplier<KubernetesConfig>(new KubernetesConfigBuilder().accept(new ApplyAutoBuild()).accept(new ApplyProjectInfo(p)));
  }

  

}
