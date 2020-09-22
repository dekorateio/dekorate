/**
 * Copyright 2018 The original authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.halkyon.handler;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.dekorate.Configurators;
import io.dekorate.Handler;
import io.dekorate.HandlerFactory;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.Resources;
import io.dekorate.WithProject;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.halkyon.config.CapabilityConfig;
import io.dekorate.halkyon.config.ComponentConfig;
import io.dekorate.halkyon.config.ComponentConfigBuilder;
import io.dekorate.halkyon.config.EditableComponentConfig;
import io.dekorate.halkyon.config.RequiredCapabilityConfig;
import io.dekorate.halkyon.decorator.AddBuildConfigToComponentDecorator;
import io.dekorate.halkyon.decorator.AddCapabilitiesToComponentDecorator;
import io.dekorate.halkyon.decorator.AddEnvToComponentDecorator;
import io.dekorate.halkyon.decorator.AddExposedPortToComponentDecorator;
import io.dekorate.halkyon.decorator.AddRuntimeTypeToComponentDecorator;
import io.dekorate.halkyon.decorator.AddRuntimeVersionToComponentDecorator;
import io.dekorate.halkyon.decorator.DeploymentModeDecorator;
import io.dekorate.halkyon.decorator.ExposeServiceDecorator;
import io.dekorate.halkyon.model.Component;
import io.dekorate.halkyon.model.ComponentBuilder;
import io.dekorate.kubernetes.config.Annotation;
import io.dekorate.kubernetes.config.BaseConfig;
import io.dekorate.kubernetes.config.ConfigKey;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.EditableBaseConfig;
import io.dekorate.kubernetes.config.Env;
import io.dekorate.kubernetes.config.Label;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.kubernetes.configurator.ApplyDeployToApplicationConfiguration;
import io.dekorate.kubernetes.decorator.AddAnnotationDecorator;
import io.dekorate.kubernetes.decorator.AddLabelDecorator;
import io.dekorate.kubernetes.decorator.AddToSelectorDecorator;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.project.Project;
import io.dekorate.project.ScmInfo;
import io.dekorate.utils.Git;
import io.dekorate.utils.Labels;
import io.dekorate.utils.Strings;

public class ComponentHandler implements HandlerFactory, Handler<ComponentConfig>, WithProject {
  public static final ConfigKey<String> RUNTIME_TYPE = new ConfigKey<>("RUNTIME_TYPE", String.class);
  public static final ConfigKey<String> RUNTIME_VERSION = new ConfigKey<>("RUNTIME_VERSION", String.class);


  private final Resources resources;
  private final Configurators configurators;
  private final Logger LOGGER = LoggerFactory.getLogger();

  public Handler create(Resources resources, Configurators configurators) {
    return new ComponentHandler(resources, configurators);
  }

  // only used for testing
  public ComponentHandler() {
    this(new Resources(), new Configurators());
  }

  public ComponentHandler(Resources resources, Configurators configurators) {
    this.resources = resources;
    this.configurators = configurators;
  }

  @Override
  public int order() {
    return 1100;
  }

  @Override
  public void handle(ComponentConfig config) {
    LOGGER.info("Processing component config.");
    if (!Strings.isNullOrEmpty(config.getName())) {
      resources.addCustom(ResourceGroup.NAME, createComponent(config));
    }
    addVisitors(config);
  }

  @Override
  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(ComponentConfig.class) ||
      type.equals(EditableComponentConfig.class);
  }

  private void addVisitors(ComponentConfig config) {
    String type = config.getAttribute(RUNTIME_TYPE);
    String version = config.getAttribute(RUNTIME_VERSION);


    generateBuildConfigIfNeeded(config);

    BaseConfig kubernetesConfig = getKubernetesConfig();
    Map<String, String> allLabels = new HashMap<>();
    allLabels.put(Labels.NAME, config.getName());

    if (Strings.isNotNullOrEmpty(config.getVersion())) {
        allLabels.put(Labels.VERSION, config.getVersion());
    }
    if (Strings.isNotNullOrEmpty(config.getPartOf())) {
       allLabels.put(Labels.PART_OF, config.getPartOf());
    }
    Arrays.stream(config.getLabels()).forEach(l -> {
            allLabels.put(l.getKey(), l.getValue());
        });

    Labels.createLabels(kubernetesConfig).forEach( (k,v) -> {
            if (!allLabels.containsKey(k)) {
                allLabels.put(k, v);
            }
        });

    allLabels.forEach( (k,v)  -> {
            resources.decorateCustom(ResourceGroup.NAME, new AddLabelDecorator(new Label(k, v)));
            resources.decorateCustom(ResourceGroup.NAME, new AddToSelectorDecorator(k, v));
        });

    for (Annotation annotation : kubernetesConfig.getAnnotations()) {
        resources.decorateCustom(ResourceGroup.NAME, new AddAnnotationDecorator(annotation));
    }
    if (config.isExposeService()) {
        resources.decorateCustom(ResourceGroup.NAME, new ExposeServiceDecorator());
        Port[] ports = kubernetesConfig.getPorts();
        if (ports.length == 0) {
            throw new IllegalStateException("Ports need to be present on KubernetesConfig");
        }
        resources.decorateCustom(ResourceGroup.NAME, new AddExposedPortToComponentDecorator(ports[0].getContainerPort()));
    }

   
    if (type != null) {
      resources.decorateCustom(ResourceGroup.NAME, new AddRuntimeTypeToComponentDecorator(type)); //
    }

    if (version != null) {
      resources.decorateCustom(ResourceGroup.NAME, new AddRuntimeVersionToComponentDecorator(version));
    }
    resources.decorateCustom(ResourceGroup.NAME, new DeploymentModeDecorator(config.getDeploymentMode()));
    for (Env env : config.getEnvs()) {
      resources.decorateCustom(ResourceGroup.NAME, new AddEnvToComponentDecorator(env));
    }
    RequiredCapabilityConfig[] requires = config.getRequires();
    CapabilityConfig[] provides = config.getProvides();
    resources.decorateCustom(ResourceGroup.NAME, new AddCapabilitiesToComponentDecorator(requires, provides));

  }

  private void generateBuildConfigIfNeeded(ComponentConfig config) {
    final ScmInfo scmInfo = config.getProject().getScmInfo();
    if (scmInfo != null) {
      String url = scmInfo.getUrl();
      // only generate buildconfig if we have a remote
      if (url != null) {
        String branch = scmInfo.getBranch();
        String buildType = config.getBuildType();
        Path modulePath = scmInfo.getRoot().relativize(config.getProject().getRoot());

        final String remote = config.getRemote();
        if (!remote.equals(Git.ORIGIN)) {
          url = Git.getSafeRemoteUrl(scmInfo.getRoot(), remote).orElse(url);
        }
        resources.decorateCustom(ResourceGroup.NAME, new AddBuildConfigToComponentDecorator(modulePath, url, branch, buildType));
      }
    }
  }

  private BaseConfig getKubernetesConfig() {
    Optional<BaseConfig> optionalKubernetesConfig = configurators.get(BaseConfig.class);
    if (optionalKubernetesConfig.isPresent()) {
      return optionalKubernetesConfig.get();
    }

    Optional<EditableBaseConfig> editableKubernetesConfig = configurators.get(EditableBaseConfig.class);
    if (editableKubernetesConfig.isPresent()) {
      return editableKubernetesConfig.get();
    }

    throw new IllegalStateException("BaseConfig needs to be present when using exposeService=true");
  }

  /**
   * Create a {@link Component} from a {@link ComponentConfig}.
   *
   * @param config The config.
   * @return The component.
   */
  private Component createComponent(ComponentConfig config) {
    return new ComponentBuilder()
      .withNewMetadata()
      .withName(config.getName())
      .endMetadata()
      .withNewSpec()
      .withDeploymentMode(config.getDeploymentMode())
      .withVersion(config.getVersion())
      .endSpec()
      .build();
  }

  @Override
  public ConfigurationSupplier<ComponentConfig> getFallbackConfig() {
    Project p = getProject();
    return new ConfigurationSupplier<ComponentConfig>(new ComponentConfigBuilder()
      .withName(p.getBuildInfo().getName())
      .accept(new ApplyDeployToApplicationConfiguration())
      .accept(new ApplyProjectInfo(p)));
  }
}
