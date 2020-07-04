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
import io.dekorate.WithProject;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.deps.knative.serving.v1.Service;
import io.dekorate.deps.knative.serving.v1.ServiceBuilder;
import io.dekorate.deps.kubernetes.api.model.KubernetesListBuilder;
import io.dekorate.knative.config.HttpTransportVersion;
import io.dekorate.knative.config.AutoScalerClass;
import io.dekorate.knative.config.AutoscalingMetric;
import io.dekorate.knative.config.EditableKnativeConfig;
import io.dekorate.knative.config.GlobalAutoScaling;
import io.dekorate.knative.config.KnativeConfig;
import io.dekorate.knative.config.KnativeConfigBuilder;
import io.dekorate.knative.decorator.ApplyGlobalAutoscalingClassDecorator;
import io.dekorate.knative.decorator.ApplyGlobalContainerConcurrencyDecorator;
import io.dekorate.knative.decorator.ApplyGlobalRequestsPerSecondTargetDecorator;
import io.dekorate.knative.decorator.ApplyLocalAutoscalingClassDecorator;
import io.dekorate.knative.decorator.ApplyLocalAutoscalingMetricDecorator;
import io.dekorate.knative.decorator.ApplyLocalAutoscalingTargetDecorator;
import io.dekorate.knative.decorator.ApplyLocalContainerConcurrencyDecorator;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.kubernetes.config.ImageConfigurationBuilder;
import io.dekorate.kubernetes.configurator.ApplyDeployToApplicationConfiguration;
import io.dekorate.kubernetes.decorator.AddConfigMapResourceProvidingDecorator;
import io.dekorate.kubernetes.decorator.ApplyPortNameDecorator;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.project.Project;
import io.dekorate.utils.Images;
import io.dekorate.utils.Labels;
import io.dekorate.utils.Ports;
import io.dekorate.utils.Strings;

public class KnativeHandler extends AbstractKubernetesHandler<KnativeConfig> implements HandlerFactory, WithProject {

  private static final String KNATIVE = "knative";
  private static final String DEFAULT_REGISTRY = "dev.local/";

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
    Optional<Service> existingService = resources.groups().getOrDefault(KNATIVE, new KubernetesListBuilder())
        .buildItems().stream().filter(i -> i instanceof Service).map(i -> (Service) i)
        .filter(i -> i.getMetadata().getName().equals(config.getName())).findAny();

    if (!existingService.isPresent()) {
      resources.add(KNATIVE, createService(config));
    }

    resources.decorate(KNATIVE,
        new ApplyPortNameDecorator(null, null, config.getHttpTransportVersion().name().toLowerCase(),
            Ports.HTTP_PORT_NAMES.toArray(new String[Ports.HTTP_PORT_NAMES.size()])));
    addDecorators(KNATIVE, config);

    if (config.getRevisionAutoScaling().getMetric() != AutoscalingMetric.concurrency) {
      resources.decorate(KNATIVE, new ApplyLocalAutoscalingMetricDecorator(config.getName(), config.getRevisionAutoScaling().getMetric()));
    }
 
    if (config.getRevisionAutoScaling().getContainerConcurrency() != 0) {
      resources.decorate(KNATIVE, new ApplyLocalContainerConcurrencyDecorator(config.getName(),
          config.getRevisionAutoScaling().getContainerConcurrency()));
    }

    // Local autoscaling configuration
    if (config.getRevisionAutoScaling().getAutoScalerClass() != AutoScalerClass.kpa) {
      resources.decorate(KNATIVE, new ApplyLocalAutoscalingClassDecorator(config.getName(), config.getRevisionAutoScaling().getAutoScalerClass()));
    }
   if (config.getRevisionAutoScaling().getTarget() != 0) {
      resources.decorate(KNATIVE, new ApplyLocalAutoscalingTargetDecorator(config.getName(), config.getRevisionAutoScaling().getTarget()));
    }
    if (config.getRevisionAutoScaling().getTarget() != 200 && config.getRevisionAutoScaling().getMetric() == AutoscalingMetric.rps) {
      resources.decorate(KNATIVE, new ApplyLocalContainerConcurrencyDecorator(config.getName(),
          config.getRevisionAutoScaling().getTarget()));
    }
    if (config.getRevisionAutoScaling().getTargetUtilizationPercentage() != 70) {
      resources.decorate(KNATIVE, new ApplyLocalContainerConcurrencyDecorator(config.getName(),
          config.getRevisionAutoScaling().getTargetUtilizationPercentage()));
    }

    // Global autoscaling configuration
    if (!isDefault(config.getGlobalAutoScaling())) {
      resources.decorate(KNATIVE, new AddConfigMapResourceProvidingDecorator("config-autoscaler"));
      if (config.getGlobalAutoScaling().getAutoScalerClass() !=  AutoScalerClass.kpa) {
        resources.decorate(KNATIVE, new ApplyGlobalAutoscalingClassDecorator(config.getGlobalAutoScaling().getAutoScalerClass()));
      }

      if (config.getGlobalAutoScaling().getRequestsPerSecond() != 200) {
        resources.decorate(KNATIVE, new ApplyGlobalRequestsPerSecondTargetDecorator(config.getGlobalAutoScaling().getRequestsPerSecond()));
      }
      if (config.getGlobalAutoScaling().getTargetUtilizationPercentage() != 70) {
        resources.decorate(KNATIVE, new ApplyGlobalContainerConcurrencyDecorator(config.getGlobalAutoScaling().getTargetUtilizationPercentage()));
      }
    }

    if (config.getGlobalAutoScaling().getContainerConcurrency() != 0) {
      resources.decorate(KNATIVE, new AddConfigMapResourceProvidingDecorator("config-defaults"));
      resources.decorate(KNATIVE, new ApplyGlobalContainerConcurrencyDecorator(config.getGlobalAutoScaling().getContainerConcurrency()));
    }
  }

  @Override
  public ConfigurationSupplier<KnativeConfig> getFallbackConfig() {
    Project p = getProject();
    return new ConfigurationSupplier<KnativeConfig>(
        new KnativeConfigBuilder().accept(new ApplyDeployToApplicationConfiguration()).accept(new ApplyProjectInfo(p)));
  }

  @Override
  protected void addDecorators(String group, KnativeConfig config) {
    super.addDecorators(group, config);
  }

  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(KnativeConfig.class) || type.equals(EditableKnativeConfig.class);
  }

  /**
   * Creates a {@link Service} for the {@link KnativeConfig}.
   * 
   * @param config The sesssion.
   * @return The deployment config.
   */
  public Service createService(KnativeConfig config) {
    ImageConfiguration imageConfig = getImageConfiguration(getProject(), config, configurators);

    String image = Strings
        .isNotNullOrEmpty(imageConfig.getImage())
            ? imageConfig.getImage()
            : Images
                .getImage(
                    imageConfig.isAutoPushEnabled()
                        ? (Strings.isNullOrEmpty(imageConfig.getRegistry()) ? DEFAULT_REGISTRY
                            : imageConfig.getRegistry())
                        : imageConfig.getRegistry(),
                    imageConfig.getGroup(), imageConfig.getName(), imageConfig.getVersion());

    return new ServiceBuilder().withNewMetadata().withName(config.getName()).withLabels(Labels.createLabels(config))
        .endMetadata().withNewSpec().withNewTemplate().withNewSpec().addNewContainer().withName(config.getName())
        .withImage(image).endContainer().endSpec().endTemplate().endSpec().build();
  }

  public static boolean isDefault(GlobalAutoScaling autoScaling) {
    if (autoScaling.getAutoScalerClass() != AutoScalerClass.kpa) {
      return false;
    }
    if (autoScaling.getContainerConcurrency() != 0) {
      return false;
    }
    if (autoScaling.getRequestsPerSecond() != 200) {
      return false;
    }
    if (autoScaling.getTargetUtilizationPercentage() != 70) {
      return false;
    }
    return true;
  }

  private static ImageConfiguration getImageConfiguration(Project project, KnativeConfig config,
      Configurators configurators) {
    return configurators.getImageConfig(BuildServiceFactories.supplierMatches(project)).map(i -> merge(config, i))
        .orElse(ImageConfiguration.from(config));
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
        .withGroup(imageConfig.getGroup() != null ? imageConfig.getGroup() : null)
        .withName(imageConfig.getName() != null ? imageConfig.getName() : config.getName())
        .withVersion(imageConfig.getVersion() != null ? imageConfig.getVersion() : config.getVersion())
        .withRegistry(imageConfig.getRegistry() != null ? imageConfig.getRegistry() : null)
        .withDockerFile(imageConfig.getDockerFile() != null ? imageConfig.getDockerFile() : null)
        .withAutoBuildEnabled(imageConfig.isAutoBuildEnabled() ? imageConfig.isAutoBuildEnabled() : false)
        .withAutoPushEnabled(imageConfig.isAutoPushEnabled() ? imageConfig.isAutoPushEnabled() : false).build();
  }
}
