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
package io.dekorate.knative.handler;

import java.util.Optional;

import io.dekorate.AbstractKubernetesHandler;
import io.dekorate.BuildServiceFactories;
import io.dekorate.Configurators;
import io.dekorate.Handler;
import io.dekorate.HandlerFactory;
import io.dekorate.Resources;
import io.dekorate.Session;
import io.dekorate.WithProject;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.deps.knative.serving.v1alpha1.Service;
import io.dekorate.deps.knative.serving.v1alpha1.ServiceBuilder;
import io.dekorate.deps.kubernetes.api.model.KubernetesListBuilder;
import io.dekorate.knative.config.EditableKnativeConfig;
import io.dekorate.knative.config.KnativeConfig;
import io.dekorate.knative.config.KnativeConfigBuilder;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.kubernetes.config.ImageConfigurationBuilder;
import io.dekorate.kubernetes.configurator.ApplyDeployToApplicationConfiguration;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.project.Project;

public class KnativeHandler extends AbstractKubernetesHandler<KnativeConfig> implements HandlerFactory, WithProject {

  private static final String KNATIVE = "knative";
  private final Configurators configurators;

  public KnativeHandler() {
    this(new Resources(), new Configurators());
  }
  public KnativeHandler(Resources resources, Configurators configurators) {
    super(resources);
    this.configurators = configurators;
  }

  @Override
  public Handler create(Resources resources, Configurators configurators) {
    return new KnativeHandler(resources, configurators);
  }

  @Override
  public int order() {
    return 400;
  }

  public void handle(KnativeConfig config) {
    setApplicationInfo(config);
    Optional<Service> existingService = resources.groups().getOrDefault(KNATIVE, new KubernetesListBuilder()).buildItems().stream()
      .filter(i -> i instanceof Service)
      .map(i -> (Service)i)
      .filter(i -> i.getMetadata().getName().equals(config.getName()))
      .findAny();

    if (!existingService.isPresent()) {
      resources.add(KNATIVE, createService(config));
    }
    addDecorators(KNATIVE, config);
  }

  @Override
  public ConfigurationSupplier<KnativeConfig> getFallbackConfig() {
    Project p = getProject();
    return new ConfigurationSupplier<KnativeConfig>(new KnativeConfigBuilder().accept(new ApplyDeployToApplicationConfiguration()).accept(new ApplyProjectInfo(p)));
  }


  @Override
  protected void addDecorators(String group, KnativeConfig config) {
    super.addDecorators(group, config);
  }

  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(KnativeConfig.class) ||
      type.equals(EditableKnativeConfig.class);
  }

  /**
   * Creates a {@link Service} for the {@link KnativeConfig}.
   * @param config   The sesssion.
   * @return          The deployment config.
   */
  public Service createService(KnativeConfig config)  {
    return new ServiceBuilder()
      .withNewMetadata()
      .withName(config.getName())
      .withLabels(resources.getLabels())
      .endMetadata()
      .withNewSpec()
      .withNewRunLatest()
      .withNewConfiguration()
      .withNewRevisionTemplate()
      .withNewSpec()
      .withNewContainer()
      .withImage("dev.local/" + config.getGroup() + "/" + config.getName() + ":" + config.getVersion())
      .endContainer()
      .endSpec()
      .endRevisionTemplate()
      .endConfiguration()
      .endRunLatest()
      .endSpec()
      .build();
  }

  private static ImageConfiguration getImageConfiguration(Project project, KnativeConfig config, Configurators configurators) {
    return configurators.get(ImageConfiguration.class, BuildServiceFactories.matches(project)).map(i -> merge(config, i)).orElse(ImageConfiguration.from(config));
  }

  private static ImageConfiguration merge(KnativeConfig config, ImageConfiguration imageConfig) {
    if (config == null) {
      throw new NullPointerException("KnativeConfig is null.");
    }
    if (imageConfig == null) {
      return ImageConfiguration.from(config);
    }
    return new ImageConfigurationBuilder()
      .withProject(imageConfig.getProject() != null ? imageConfig.getProject() : config.getProject())
      .withGroup(imageConfig.getGroup() != null ? imageConfig.getGroup() : config.getGroup())
      .withName(imageConfig.getName() != null ? imageConfig.getName() : config.getName())
      .withVersion(imageConfig.getVersion() != null ? imageConfig.getVersion() : config.getVersion())
      .withRegistry(imageConfig.getRegistry() != null ? imageConfig.getRegistry() : null)
      .withDockerFile(imageConfig.getDockerFile() != null ? imageConfig.getDockerFile() : null)
      .withAutoBuildEnabled(imageConfig.isAutoBuildEnabled() ? imageConfig.isAutoBuildEnabled() : false)
      .withAutoPushEnabled(imageConfig.isAutoPushEnabled() ? imageConfig.isAutoPushEnabled() : false)
      .build();
  }
}
