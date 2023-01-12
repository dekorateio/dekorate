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
package io.dekorate.kubernetes.manifest;

import java.util.Optional;

import io.dekorate.AbstractKubernetesManifestGenerator;
import io.dekorate.ConfigurationRegistry;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.ResourceRegistry;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.Container;
import io.dekorate.kubernetes.config.ContainerBuilder;
import io.dekorate.kubernetes.config.EditableKubernetesConfig;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.kubernetes.config.IngressRule;
import io.dekorate.kubernetes.config.IngressRuleBuilder;
import io.dekorate.kubernetes.config.KubernetesConfig;
import io.dekorate.kubernetes.config.KubernetesConfigBuilder;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.kubernetes.configurator.ApplyDeployToApplicationConfiguration;
import io.dekorate.kubernetes.decorator.AddCommitIdAnnotationDecorator;
import io.dekorate.kubernetes.decorator.AddIngressDecorator;
import io.dekorate.kubernetes.decorator.AddIngressRuleDecorator;
import io.dekorate.kubernetes.decorator.AddIngressTlsDecorator;
import io.dekorate.kubernetes.decorator.AddInitContainerDecorator;
import io.dekorate.kubernetes.decorator.AddServiceResourceDecorator;
import io.dekorate.kubernetes.decorator.AddVcsUrlAnnotationDecorator;
import io.dekorate.kubernetes.decorator.ApplyApplicationContainerDecorator;
import io.dekorate.kubernetes.decorator.ApplyDeploymentStrategyDecorator;
import io.dekorate.kubernetes.decorator.ApplyHeadlessDecorator;
import io.dekorate.kubernetes.decorator.ApplyImageDecorator;
import io.dekorate.kubernetes.decorator.ApplyReplicasToDeploymentDecorator;
import io.dekorate.kubernetes.decorator.ApplyReplicasToStatefulSetDecorator;
import io.dekorate.kubernetes.decorator.StatefulSetResourceFactory;
import io.dekorate.option.config.VcsConfig;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.project.Project;
import io.dekorate.utils.Annotations;
import io.dekorate.utils.Git;
import io.dekorate.utils.Images;
import io.dekorate.utils.Labels;
import io.dekorate.utils.Ports;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;

public class KubernetesManifestGenerator extends AbstractKubernetesManifestGenerator<KubernetesConfig> {

  private static final String KUBERNETES = "kubernetes";
  private static final String DEFAULT_REGISTRY = "docker.io";

  private static final String KUBERNETES_NAMESPACE = "KUBERNETES_NAMESPACE";
  private static final String METADATA_NAMESPACE = "metadata.namespace";

  private final Logger LOGGER = LoggerFactory.getLogger();

  public KubernetesManifestGenerator(ResourceRegistry resources, ConfigurationRegistry configurators) {
    super(resources, configurators);
    resources.groups().putIfAbsent(KUBERNETES, new KubernetesListBuilder());
  }

  @Override
  public String getKey() {
    return KUBERNETES;
  }

  @Override
  public int order() {
    return 200;
  }

  public void generate(KubernetesConfig config) {
    LOGGER.info("Processing kubernetes configuration.");
    initializeRegistry(config);

    addDecorators(KUBERNETES, config);
  }

  public boolean accepts(Class<? extends Configuration> type) {
    return type.equals(KubernetesConfig.class) || type.equals(EditableKubernetesConfig.class);
  }

  @Override
  protected void addDecorators(String group, KubernetesConfig config) {
    super.addDecorators(group, config);

    ImageConfiguration imageConfig = getImageConfiguration(config);
    String image = Strings.isNotNullOrEmpty(imageConfig.getImage())
        ? imageConfig.getImage()
        : Images.getImage(imageConfig.isAutoPushEnabled()
            ? (Strings.isNullOrEmpty(imageConfig.getRegistry()) ? DEFAULT_REGISTRY : imageConfig.getRegistry())
            : imageConfig.getRegistry(),
            imageConfig.getGroup(), imageConfig.getName(), imageConfig.getVersion());

    Container appContainer = new ContainerBuilder()
        .withName(config.getName())
        .withImage(image)
        .addNewEnvVar()
        .withName(KUBERNETES_NAMESPACE)
        .withField(METADATA_NAMESPACE)
        .endEnvVar()
        .build();

    Project project = getProject();
    Optional<VcsConfig> vcsConfig = configurationRegistry.get(VcsConfig.class);
    String remote = vcsConfig.map(VcsConfig::getRemote).orElse(Git.ORIGIN);
    boolean httpsPreferred = vcsConfig.map(VcsConfig::isHttpsPreferred).orElse(false);

    String vcsUrl = project.getScmInfo() != null && Strings.isNotNullOrEmpty(project.getScmInfo().getRemote().get(Git.ORIGIN))
        ? Git.getRemoteUrl(project.getRoot(), remote, httpsPreferred).orElse(Labels.UNKNOWN)
        : Labels.UNKNOWN;

    resourceRegistry.decorate(group, new AddVcsUrlAnnotationDecorator(config.getName(), Annotations.VCS_URL, vcsUrl));
    resourceRegistry.decorate(group, new AddCommitIdAnnotationDecorator());

    resourceRegistry.decorate(group, new ApplyApplicationContainerDecorator(config.getName(), appContainer));
    resourceRegistry.decorate(group, new ApplyImageDecorator(config.getName(), image));

    for (Container container : config.getInitContainers()) {
      resourceRegistry.decorate(group, new AddInitContainerDecorator(config.getName(), container));
    }

    if (config.getPorts().length > 0) {
      resourceRegistry.decorate(group, new AddServiceResourceDecorator(config));
    }

    resourceRegistry.decorate(group, new AddIngressDecorator(config, Labels.createLabelsAsMap(config, "Ingress")));
    Optional<Port> defaultHostPort = Ports
        .getPortByFilter(p -> Strings.equals(p.getName(), config.getIngress().getTargetPort()), config);
    defaultHostPort.ifPresent(port -> {
      resourceRegistry.decorate(group,
          new AddIngressRuleDecorator(config.getName(), defaultHostPort, new IngressRuleBuilder()
              .withHost(config.getIngress().getHost())
              .withPath(port.getPath())
              .withServicePortName(port.getName())
              .withServicePortNumber(port.getHostPort()).build()));
    });

    if (config.getIngress() != null) {
      if (Strings.isNotNullOrEmpty(config.getIngress().getTlsSecretName())) {
        resourceRegistry.decorate(group, new AddIngressTlsDecorator(config.getName(), config.getIngress()));
      }

      if (config.getIngress().getRules() != null) {
        for (IngressRule ingressRule : config.getIngress().getRules()) {
          resourceRegistry.decorate(group, new AddIngressRuleDecorator(config.getName(), defaultHostPort, ingressRule));
        }
      }
    }

    if (config.isHeadless()) {
      resourceRegistry.decorate(KUBERNETES, new ApplyHeadlessDecorator(config.getName()));
    }

    if (config.getReplicas() != null && config.getReplicas() != 1) {
      if (StatefulSetResourceFactory.KIND.equalsIgnoreCase(config.getDeploymentKind())) {
        resourceRegistry.decorate(KUBERNETES, new ApplyReplicasToStatefulSetDecorator(config.getName(), config.getReplicas()));
      } else {
        resourceRegistry.decorate(KUBERNETES, new ApplyReplicasToDeploymentDecorator(config.getName(), config.getReplicas()));
      }
    }

    resourceRegistry.decorate(KUBERNETES, new ApplyDeploymentStrategyDecorator(config.getName(), config.getDeploymentStrategy(),
        config.getRollingUpdate()));
  }

  @Override
  public ConfigurationSupplier<KubernetesConfig> getFallbackConfig() {
    Project p = getProject();
    return new ConfigurationSupplier<KubernetesConfig>(new KubernetesConfigBuilder()
        .accept(new ApplyDeployToApplicationConfiguration()).accept(new ApplyProjectInfo(p)));
  }
}
