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

import java.util.Arrays;
import java.util.Optional;

import io.dekorate.AbstractKubernetesManifestGenerator;
import io.dekorate.ConfigurationRegistry;
import io.dekorate.ResourceRegistry;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.knative.config.AutoScalerClass;
import io.dekorate.knative.config.AutoScaling;
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
import io.dekorate.knative.decorator.AddEmptyDirVolumeToRevisionDecorator;
import io.dekorate.knative.decorator.AddHostAliasesToRevisionDecorator;
import io.dekorate.knative.decorator.AddNodeSelectorToRevisionDecorator;
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
import io.dekorate.knative.decorator.ApplyLocalTargetUtilizationPercentageDecorator;
import io.dekorate.knative.decorator.ApplyMaxScaleDecorator;
import io.dekorate.knative.decorator.ApplyMinScaleDecorator;
import io.dekorate.knative.decorator.ApplyRevisionNameDecorator;
import io.dekorate.knative.decorator.ApplyServiceAccountToRevisionSpecDecorator;
import io.dekorate.knative.decorator.ApplyTrafficDecorator;
import io.dekorate.kubernetes.config.AwsElasticBlockStoreVolume;
import io.dekorate.kubernetes.config.AzureDiskVolume;
import io.dekorate.kubernetes.config.AzureFileVolume;
import io.dekorate.kubernetes.config.ConfigMapVolume;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.Container;
import io.dekorate.kubernetes.config.EmptyDirVolume;
import io.dekorate.kubernetes.config.HostAlias;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.kubernetes.config.LabelBuilder;
import io.dekorate.kubernetes.config.PersistentVolumeClaimVolume;
import io.dekorate.kubernetes.config.SecretVolume;
import io.dekorate.kubernetes.configurator.ApplyDeployToApplicationConfiguration;
import io.dekorate.kubernetes.decorator.AddCommitIdAnnotationDecorator;
import io.dekorate.kubernetes.decorator.AddConfigMapDataDecorator;
import io.dekorate.kubernetes.decorator.AddConfigMapResourceProvidingDecorator;
import io.dekorate.kubernetes.decorator.AddImagePullSecretToServiceAccountDecorator;
import io.dekorate.kubernetes.decorator.AddLabelDecorator;
import io.dekorate.kubernetes.decorator.AddServiceAccountResourceDecorator;
import io.dekorate.kubernetes.decorator.AddVcsUrlAnnotationDecorator;
import io.dekorate.kubernetes.decorator.ApplyImagePullPolicyDecorator;
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

public class KnativeManifestGenerator extends AbstractKubernetesManifestGenerator<KnativeConfig> {

  public static final String CONFIG_AUTOSCALER = "config-autoscaler";
  public static final String CONFIG_DEFAULTS = "config-defaults";

  private static final KnativeConfig DEFAULT_KNATIVE_CONFIG = KnativeConfig.newKnativeConfigBuilderFromDefaults().build();
  private static final GlobalAutoScaling DEFAULT_GLOBAL_AUTOSCALING = GlobalAutoScaling.newBuilderFromDefaults().build();
  private static final AutoScaling DEFAULT_AUTOSCALING = AutoScaling.newBuilderFromDefaults().build();

  private static final Traffic DEFAULT_TRAFFIC = Traffic.newBuilderFromDefaults().build();

  private static final String KNATIVE = "knative";
  private static final String KNATIVE_SERVING = "knative-serving";
  private static final String DEFAULT_REGISTRY = "dev.local/";

  private static final String KNATIVE_VISIBILITY = "networking.knative.dev/visibility";
  private static final String CLUSTER_LOCAL = "cluster-local";

  public KnativeManifestGenerator() {
    this(new ResourceRegistry(), new ConfigurationRegistry());
  }

  public KnativeManifestGenerator(ResourceRegistry resourceRegistry, ConfigurationRegistry configurationRegistry) {
    super(resourceRegistry, configurationRegistry);
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
        new ApplyPortNameDecorator(config.getName(), null,
            config.getHttpTransportVersion() != null ? config.getHttpTransportVersion().name().toLowerCase() : "http1",
            Ports.webPortNames().toArray(new String[Ports.webPortNames().size()])));
    addDecorators(KNATIVE, config);

    if (config.getMinScale() != null && config.getMinScale() != 0) {
      resourceRegistry.decorate(KNATIVE, new ApplyMinScaleDecorator(config.getName(), config.getMinScale()));
    }

    if (config.getMaxScale() != null && config.getMaxScale() != 0) {
      resourceRegistry.decorate(KNATIVE, new ApplyMaxScaleDecorator(config.getName(), config.getMaxScale()));
    }

    if (!config.isScaleToZeroEnabled()) {
      resourceRegistry.decorate(KNATIVE, new AddConfigMapResourceProvidingDecorator(CONFIG_AUTOSCALER, KNATIVE_SERVING));
      resourceRegistry.decorate(KNATIVE, new AddConfigMapDataDecorator(CONFIG_AUTOSCALER, "enable-scale-to-zero",
          String.valueOf(config.isScaleToZeroEnabled())));
    }

    if (Strings.isNotNullOrEmpty(config.getRevisionName())) {
      resourceRegistry.decorate(KNATIVE, new ApplyRevisionNameDecorator(config.getName(), config.getRevisionName()));
    }

    if (config.getRevisionAutoScaling() != null) {
      if (config.getRevisionAutoScaling().getMetric() != null
          && config.getRevisionAutoScaling().getMetric() != AutoscalingMetric.concurrency) {
        resourceRegistry.decorate(KNATIVE,
            new ApplyLocalAutoscalingMetricDecorator(config.getName(), config.getRevisionAutoScaling().getMetric()));
      }

      // Local autoscaling configuration
      if (config.getRevisionAutoScaling().getAutoScalerClass() != null
          && config.getRevisionAutoScaling().getAutoScalerClass() != AutoScalerClass.kpa) {
        resourceRegistry.decorate(KNATIVE, new ApplyLocalAutoscalingClassDecorator(config.getName(),
            config.getRevisionAutoScaling().getAutoScalerClass()));
      }

      if (config.getRevisionAutoScaling().getTarget() != null
          && config.getRevisionAutoScaling().getTarget().intValue() != DEFAULT_AUTOSCALING.getTarget().intValue()) {
        resourceRegistry.decorate(KNATIVE,
            new ApplyLocalAutoscalingTargetDecorator(config.getName(), config.getRevisionAutoScaling().getTarget()));
      }

      // Hard Limit
      if (config.getRevisionAutoScaling().getContainerConcurrency() != null
          && config.getRevisionAutoScaling().getContainerConcurrency().intValue() != DEFAULT_AUTOSCALING
              .getContainerConcurrency().intValue()) {
        resourceRegistry.decorate(KNATIVE, new ApplyLocalContainerConcurrencyDecorator(config.getName(),
            config.getRevisionAutoScaling().getContainerConcurrency()));
      }

      // Soft Limit
      if (config.getRevisionAutoScaling().getTarget() != null
          && config.getRevisionAutoScaling().getTarget().intValue() != DEFAULT_AUTOSCALING.getTarget().intValue()
          && config.getRevisionAutoScaling().getMetric() == AutoscalingMetric.rps) {
        resourceRegistry.decorate(KNATIVE, new ApplyLocalContainerConcurrencyDecorator(config.getName(),
            config.getRevisionAutoScaling().getTarget()));
      }

      if (config.getRevisionAutoScaling().getTargetUtilizationPercentage() != null
          && config.getRevisionAutoScaling().getTargetUtilizationPercentage().intValue() != DEFAULT_AUTOSCALING
              .getTargetUtilizationPercentage().intValue()) {
        resourceRegistry.decorate(KNATIVE, new ApplyLocalTargetUtilizationPercentageDecorator(config.getName(),
            config.getRevisionAutoScaling().getTargetUtilizationPercentage()));
      }
    }

    // Global autoscaling configuration
    if (config.getGlobalAutoScaling() != null) {
      if (config.getGlobalAutoScaling().getAutoScalerClass() != null
          && config.getGlobalAutoScaling().getAutoScalerClass() != AutoScalerClass.kpa) {
        resourceRegistry.decorate(KNATIVE, new AddConfigMapResourceProvidingDecorator(CONFIG_AUTOSCALER, KNATIVE_SERVING));
        resourceRegistry.decorate(KNATIVE,
            new ApplyGlobalAutoscalingClassDecorator(config.getGlobalAutoScaling().getAutoScalerClass()));
      }

      if (config.getGlobalAutoScaling().getRequestsPerSecond() != null && config.getGlobalAutoScaling().getRequestsPerSecond()
          .intValue() != DEFAULT_GLOBAL_AUTOSCALING.getRequestsPerSecond().intValue()) {
        resourceRegistry.decorate(KNATIVE, new AddConfigMapResourceProvidingDecorator(CONFIG_AUTOSCALER, KNATIVE_SERVING));
        resourceRegistry.decorate(KNATIVE,
            new ApplyGlobalRequestsPerSecondTargetDecorator(config.getGlobalAutoScaling().getRequestsPerSecond()));
      }
      if (config.getGlobalAutoScaling().getTargetUtilizationPercentage() != null
          && config.getGlobalAutoScaling().getTargetUtilizationPercentage().intValue() != DEFAULT_GLOBAL_AUTOSCALING
              .getTargetUtilizationPercentage().intValue()) {
        resourceRegistry.decorate(KNATIVE, new AddConfigMapResourceProvidingDecorator(CONFIG_DEFAULTS, KNATIVE_SERVING));
        resourceRegistry.decorate(KNATIVE, new ApplyGlobalContainerConcurrencyDecorator(
            config.getGlobalAutoScaling().getTargetUtilizationPercentage()));
      }

      if (config.getGlobalAutoScaling().getContainerConcurrency() != null
          && config.getGlobalAutoScaling().getContainerConcurrency().intValue() != DEFAULT_GLOBAL_AUTOSCALING
              .getContainerConcurrency().intValue()) {
        resourceRegistry.decorate(KNATIVE, new AddConfigMapResourceProvidingDecorator(CONFIG_DEFAULTS, KNATIVE_SERVING));
        resourceRegistry.decorate(KNATIVE,
            new ApplyGlobalContainerConcurrencyDecorator(config.getGlobalAutoScaling().getContainerConcurrency()));
      }
    }

    for (Traffic traffic : config.getTraffic()) {
      String revisionName = Strings.isNotNullOrEmpty(config.getRevisionName()) ? config.getRevisionName() : null;
      String tag = Strings.isNotNullOrEmpty(traffic.getTag()) ? traffic.getTag() : null;
      boolean latestRevision = revisionName == null ? true : traffic.isLatestRevision();
      long percentage = traffic.getPercentage();
      resourceRegistry.decorate(KNATIVE,
          new ApplyTrafficDecorator(config.getName(), revisionName, latestRevision, percentage, tag));
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

    for (EmptyDirVolume volume : config.getEmptyDirVolumes()) {
      resourceRegistry.decorate(KNATIVE, new AddEmptyDirVolumeToRevisionDecorator(volume));
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

    if (config.getNodeSelector() != null) {
      resourceRegistry.decorate(KNATIVE, new AddNodeSelectorToRevisionDecorator(config.getNodeSelector()));
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
    if (config.getImagePullPolicy() != DEFAULT_KNATIVE_CONFIG.getImagePullPolicy()) {
      resourceRegistry.decorate(group, new ApplyImagePullPolicyDecorator(config.getName(), config.getImagePullPolicy()));
    }

    if (!config.isExpose()) {
      resourceRegistry.decorate(group, new AddLabelDecorator(config.getName(), new LabelBuilder()
          .withKey(KNATIVE_VISIBILITY)
          .withValue(CLUSTER_LOCAL)
          .build()));
    }

    if (config.getImagePullSecrets() != null && config.getImagePullSecrets().length != 0) {
      String serviceAccount = Strings.isNotNullOrEmpty(config.getServiceAccount()) ? config.getServiceAccount()
          : config.getName();
      resourceRegistry.decorate(group, new AddServiceAccountResourceDecorator(config.getName(), serviceAccount));
      resourceRegistry.decorate(group, new ApplyServiceAccountToRevisionSpecDecorator(config.getName(), serviceAccount));
      resourceRegistry.decorate(group,
          new AddImagePullSecretToServiceAccountDecorator(serviceAccount, Arrays.asList(config.getImagePullSecrets())));
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
    ImageConfiguration imageConfig = getImageConfiguration(config);

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
        .endMetadata().withNewSpec().withNewTemplate().withNewSpec()
        .addNewContainer().withName(config.getName())
        .withImage(image).endContainer().endSpec().endTemplate().endSpec().build();
  }
}
