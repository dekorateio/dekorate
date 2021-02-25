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
package io.dekorate.knative.manifest;

import java.util.Optional;

import io.dekorate.AbstractKubernetesManifestGenerator;
import io.dekorate.BuildServiceFactories;
import io.dekorate.ConfigurationRegistry;
import io.dekorate.ManifestGenerator;
import io.dekorate.ManifestGeneratorFactory;
import io.dekorate.ResourceRegistry;
import io.dekorate.WithProject;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.knative.config.AutoScalerClass;
import io.dekorate.knative.config.AutoscalingMetric;
import io.dekorate.knative.config.EditableKnativeConfig;
import io.dekorate.knative.config.GlobalAutoScaling;
import io.dekorate.knative.config.KnativeConfig;
import io.dekorate.knative.config.KnativeConfigBuilder;
import io.dekorate.knative.config.Traffic;
import io.dekorate.knative.decorator.AddAwsElasticBlockStoreVolumeToRevisionDecorator;
import io.dekorate.knative.decorator.AddAzureDiskVolumeToRevisionDecorator;
import io.dekorate.knative.decorator.AddAzureFileVolumeToRevisionDecorator;
import io.dekorate.knative.decorator.AddConfigMapVolumeToRevisionDecorator;
import io.dekorate.knative.decorator.AddHostAliasesToRevisionDecorator;
import io.dekorate.knative.decorator.AddPvcVolumeToRevisionDecorator;
import io.dekorate.knative.decorator.AddSecretVolumeToRevisionDecorator;
import io.dekorate.knative.decorator.AddSidecarToRevisionDecorator;
import io.dekorate.knative.decorator.ApplyGlobalAutoscalingClassDecorator;
import io.dekorate.knative.decorator.ApplyGlobalContainerConcurrencyDecorator;
import io.dekorate.knative.decorator.ApplyGlobalRequestsPerSecondTargetDecorator;
import io.dekorate.knative.decorator.ApplyLocalAutoscalingClassDecorator;
import io.dekorate.knative.decorator.ApplyLocalAutoscalingMetricDecorator;
import io.dekorate.knative.decorator.ApplyLocalAutoscalingTargetDecorator;
import io.dekorate.knative.decorator.ApplyLocalContainerConcurrencyDecorator;
import io.dekorate.knative.decorator.ApplyMaxScaleDecorator;
import io.dekorate.knative.decorator.ApplyMinScaleDecorator;
import io.dekorate.knative.decorator.ApplyRevisionNameDecorator;
import io.dekorate.knative.decorator.ApplyTrafficDecorator;
import io.dekorate.kubernetes.config.AwsElasticBlockStoreVolume;
import io.dekorate.kubernetes.config.AzureDiskVolume;
import io.dekorate.kubernetes.config.AzureFileVolume;
import io.dekorate.kubernetes.config.ConfigMapVolume;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.Container;
import io.dekorate.kubernetes.config.HostAlias;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.kubernetes.config.ImageConfigurationBuilder;
import io.dekorate.kubernetes.config.LabelBuilder;
import io.dekorate.kubernetes.config.PersistentVolumeClaimVolume;
import io.dekorate.kubernetes.config.SecretVolume;
import io.dekorate.kubernetes.configurator.ApplyDeployToApplicationConfiguration;
import io.dekorate.kubernetes.decorator.AddCommitIdAnnotationDecorator;
import io.dekorate.kubernetes.decorator.AddConfigMapDataDecorator;
import io.dekorate.kubernetes.decorator.AddConfigMapResourceProvidingDecorator;
import io.dekorate.kubernetes.decorator.AddLabelDecorator;
import io.dekorate.kubernetes.decorator.AddVcsUrlAnnotationDecorator;
import io.dekorate.kubernetes.decorator.ApplyPortNameDecorator;
import io.dekorate.option.config.VcsConfig;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.project.Project;
import io.dekorate.utils.Annotations;
import io.dekorate.utils.Git;
import io.dekorate.utils.Images;
import io.dekorate.utils.Labels;
import io.dekorate.utils.Ports;
import io.dekorate.utils.Strings;
import io.fabric8.knative.serving.v1.Service;
import io.fabric8.knative.serving.v1.ServiceBuilder;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;

public class KnativeManifestGenerator extends AbstractKubernetesManifestGenerator<KnativeConfig> implements WithProject {

  private static final String KNATIVE = "knative";
  private static final String DEFAULT_REGISTRY = "dev.local/";

  private static final String KNATIVE_VISIBILITY = "serving.knative.dev/visibility";
  private static final String CLUSTER_LOCAL = "cluster-local";

  private final ConfigurationRegistry configurationRegistry;

  public KnativeManifestGenerator() {
    this(new ResourceRegistry(), new ConfigurationRegistry());
  }

  public KnativeManifestGenerator(ResourceRegistry resourceRegistry, ConfigurationRegistry configurationRegistry) {
    super(resourceRegistry);
    this.configurationRegistry = configurationRegistry;
  }

  @Override
  public int order() {
    return 400;
  }

  @Override
  public String getKey() {
    return KNATIVE;
  }

  public void generate(KnativeConfig config) {
    Optional<Service> existingService = resourceRegistry.groups().getOrDefault(KNATIVE, new KubernetesListBuilder())
        .buildItems().stream().filter(i -> i instanceof Service).map(i -> (Service) i)
        .filter(i -> i.getMetadata().getName().equals(config.getName())).findAny();

    if (!existingService.isPresent()) {
      resourceRegistry.add(KNATIVE, createService(config));
    }

    Project project = getProject();
    Optional<VcsConfig> vcsConfig = configurationRegistry.get(VcsConfig.class);
    String remote = vcsConfig.map(VcsConfig::getRemote).orElse(Git.ORIGIN);
    boolean httpsPrefered = vcsConfig.map(VcsConfig::isHttpsPreferred).orElse(false);

    String vcsUrl = project.getScmInfo() != null && Strings.isNotNullOrEmpty(project.getScmInfo().getRemote().get(Git.ORIGIN))
      ? Git.getRemoteUrl(project.getRoot(), remote, httpsPrefered).orElse(Labels.UNKNOWN)
      : Labels.UNKNOWN;

    resourceRegistry.decorate(KNATIVE, new AddVcsUrlAnnotationDecorator(config.getName(), Annotations.VCS_URL, vcsUrl));
    resourceRegistry.decorate(KNATIVE, new AddCommitIdAnnotationDecorator());

    resourceRegistry.decorate(KNATIVE,
        new ApplyPortNameDecorator(null, null, config.getHttpTransportVersion().name().toLowerCase(),
                                   Ports.webPortNames().toArray(new String[Ports.webPortNames().size()])));
    addDecorators(KNATIVE, config);

    if (config.getRevisionAutoScaling().getMetric() != AutoscalingMetric.concurrency) {
      resourceRegistry.decorate(KNATIVE,
          new ApplyLocalAutoscalingMetricDecorator(config.getName(), config.getRevisionAutoScaling().getMetric()));
    }

    if (config.getRevisionAutoScaling().getContainerConcurrency() != 0) {
      resourceRegistry.decorate(KNATIVE, new ApplyLocalContainerConcurrencyDecorator(config.getName(),
          config.getRevisionAutoScaling().getContainerConcurrency()));
    }

    // Local autoscaling configuration
    if (config.getRevisionAutoScaling().getAutoScalerClass() != AutoScalerClass.kpa) {
      resourceRegistry.decorate(KNATIVE, new ApplyLocalAutoscalingClassDecorator(config.getName(),
          config.getRevisionAutoScaling().getAutoScalerClass()));
    }
    if (config.getRevisionAutoScaling().getTarget() != 0) {
      resourceRegistry.decorate(KNATIVE,
          new ApplyLocalAutoscalingTargetDecorator(config.getName(), config.getRevisionAutoScaling().getTarget()));
    }
    if (config.getRevisionAutoScaling().getTarget() != 200
        && config.getRevisionAutoScaling().getMetric() == AutoscalingMetric.rps) {
      resourceRegistry.decorate(KNATIVE, new ApplyLocalContainerConcurrencyDecorator(config.getName(),
          config.getRevisionAutoScaling().getTarget()));
    }
    if (config.getRevisionAutoScaling().getTargetUtilizationPercentage() != 70) {
      resourceRegistry.decorate(KNATIVE, new ApplyLocalContainerConcurrencyDecorator(config.getName(),
          config.getRevisionAutoScaling().getTargetUtilizationPercentage()));
    }

    if (config.getMinScale() != 0) {
      resourceRegistry.decorate(KNATIVE, new ApplyMinScaleDecorator(config.getName(), config.getMinScale()));
    }

    if (config.getMaxScale() != 0) {
      resourceRegistry.decorate(KNATIVE, new ApplyMaxScaleDecorator(config.getName(), config.getMaxScale()));
    }

    // Global autoscaling configuration
    if (!isDefault(config.getGlobalAutoScaling())) {
      resourceRegistry.decorate(KNATIVE, new AddConfigMapResourceProvidingDecorator("config-autoscaler"));
      if (config.getGlobalAutoScaling().getAutoScalerClass() != AutoScalerClass.kpa) {
        resourceRegistry.decorate(KNATIVE,
            new ApplyGlobalAutoscalingClassDecorator(config.getGlobalAutoScaling().getAutoScalerClass()));
      }

      if (config.getGlobalAutoScaling().getRequestsPerSecond() != 200) {
        resourceRegistry.decorate(KNATIVE,
            new ApplyGlobalRequestsPerSecondTargetDecorator(config.getGlobalAutoScaling().getRequestsPerSecond()));
      }
      if (config.getGlobalAutoScaling().getTargetUtilizationPercentage() != 70) {
        resourceRegistry.decorate(KNATIVE, new ApplyGlobalContainerConcurrencyDecorator(
            config.getGlobalAutoScaling().getTargetUtilizationPercentage()));
      }

    }

    if (config.getGlobalAutoScaling().getContainerConcurrency() != 0) {
      resourceRegistry.decorate(KNATIVE, new AddConfigMapResourceProvidingDecorator("config-defaults"));
      resourceRegistry.decorate(KNATIVE,
          new ApplyGlobalContainerConcurrencyDecorator(config.getGlobalAutoScaling().getContainerConcurrency()));
    }

    if (!config.isScaleToZeroEnabled()) {
      resourceRegistry.decorate(KNATIVE, new AddConfigMapResourceProvidingDecorator("config-autoscaler"));
      resourceRegistry.decorate(KNATIVE, new AddConfigMapDataDecorator("config-autoscaler", "enable-scale-to-zero",
          String.valueOf(config.isAutoDeployEnabled())));
    }

    if (Strings.isNotNullOrEmpty(config.getRevisionName())) {
      resourceRegistry.decorate(KNATIVE, new ApplyRevisionNameDecorator(config.getName(), config.getRevisionName()));
    }

    for (Traffic traffic: config.getTraffic()) {
      String revisionName = Strings.isNotNullOrEmpty(config.getRevisionName()) ? config.getRevisionName() : null;
      String tag = Strings.isNotNullOrEmpty(traffic.getTag()) ? traffic.getTag() : null;
      boolean latestRevision =  revisionName == null ? true : traffic.isLatestRevision();
      long percentage = traffic.getPercentage();
      resourceRegistry.decorate(KNATIVE, new ApplyTrafficDecorator(config.getName(), revisionName, latestRevision, percentage, tag));
    }


    //Revision specific stuff
    for (Container container : config.getSidecars()) {
      resourceRegistry.decorate(KNATIVE, new AddSidecarToRevisionDecorator(config.getName(), container));
    }
 
    for (SecretVolume volume : config.getSecretVolumes()) {
      validateVolume(volume);
      resourceRegistry.decorate(KNATIVE, new AddSecretVolumeToRevisionDecorator(volume));
    }

    for (ConfigMapVolume volume : config.getConfigMapVolumes()) {
      validateVolume(volume);
      resourceRegistry.decorate(KNATIVE, new AddConfigMapVolumeToRevisionDecorator(volume));
    }

    for (PersistentVolumeClaimVolume volume : config.getPvcVolumes()) {
      resourceRegistry.decorate(KNATIVE, new AddPvcVolumeToRevisionDecorator(volume));
    }

    for (AzureFileVolume volume : config.getAzureFileVolumes()) {
      resourceRegistry.decorate(KNATIVE, new AddAzureFileVolumeToRevisionDecorator(volume));
    }

    for (AzureDiskVolume volume : config.getAzureDiskVolumes()) {
      resourceRegistry.decorate(KNATIVE, new AddAzureDiskVolumeToRevisionDecorator(volume));
    }

    for (AwsElasticBlockStoreVolume volume : config.getAwsElasticBlockStoreVolumes()) {
      resourceRegistry.decorate(KNATIVE, new AddAwsElasticBlockStoreVolumeToRevisionDecorator(volume));
    }

    for (HostAlias hostAlias : config.getHostAliases()) {
      resourceRegistry.decorate(KNATIVE, new AddHostAliasesToRevisionDecorator(hostAlias));
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
    if (!config.isExpose()) {
      resourceRegistry.decorate(group, new AddLabelDecorator(config.getName(), new LabelBuilder()
          .withKey(KNATIVE_VISIBILITY)
          .withValue(CLUSTER_LOCAL)
          .build()));
    }
  }

  public boolean accepts(Class<? extends Configuration> type) {
    return type.equals(KnativeConfig.class) || type.equals(EditableKnativeConfig.class);
  }

  /**
   * Creates a {@link Service} for the {@link KnativeConfig}.
   * 
   * @param config The sesssion.
   * @return The deployment config.
   */
  public Service createService(KnativeConfig config) {
    ImageConfiguration imageConfig = getImageConfiguration(getProject(), config, configurationRegistry);

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

    return new ServiceBuilder().withNewMetadata().withName(config.getName())
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
      ConfigurationRegistry configurationRegistry) {
    return configurationRegistry.getImageConfig(BuildServiceFactories.supplierMatches(project)).map(i -> merge(config, i))
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
        .withImage(imageConfig.getImage() != null ? imageConfig.getImage() : null)
        .withGroup(imageConfig.getGroup() != null ? imageConfig.getGroup() : null)
        .withName(imageConfig.getName() != null ? imageConfig.getName() : config.getName())
        .withVersion(imageConfig.getVersion() != null ? imageConfig.getVersion() : config.getVersion())
        .withRegistry(imageConfig.getRegistry() != null ? imageConfig.getRegistry() : null)
        .withDockerFile(imageConfig.getDockerFile() != null ? imageConfig.getDockerFile() : null)
        .withAutoBuildEnabled(imageConfig.isAutoBuildEnabled() ? imageConfig.isAutoBuildEnabled() : false)
        .withAutoPushEnabled(imageConfig.isAutoPushEnabled() ? imageConfig.isAutoPushEnabled() : false).build();
  }
}
