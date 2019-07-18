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
package io.dekorate.component.handler;

import io.dekorate.Configurators;
import io.dekorate.Handler;
import io.dekorate.HandlerFactory;
import io.dekorate.Resources;
import io.dekorate.WithProject;
import io.dekorate.component.config.ComponentConfig;
import io.dekorate.component.config.ComponentConfigBuilder;
import io.dekorate.component.config.EditableComponentConfig;
import io.dekorate.component.decorator.AddBuildConfigToComponentDecorator;
import io.dekorate.component.decorator.AddEnvToComponentDecorator;
import io.dekorate.component.decorator.AddExposedPortToComponentDecorator;
import io.dekorate.component.decorator.AddRuntimeTypeToComponentDecorator;
import io.dekorate.component.decorator.AddRuntimeVersionToComponentDecorator;
import io.dekorate.component.decorator.DeploymentModeDecorator;
import io.dekorate.component.decorator.ExposeServiceDecorator;
import io.dekorate.component.model.Component;
import io.dekorate.component.model.ComponentBuilder;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.kubernetes.config.BaseConfig;
import io.dekorate.kubernetes.config.ConfigKey;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.EditableBaseConfig;
import io.dekorate.kubernetes.config.Env;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.kubernetes.configurator.ApplyAutoBuild;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.project.Project;
import io.dekorate.utils.Labels;
import io.dekorate.utils.Strings;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class ComponentHandler implements HandlerFactory, Handler<ComponentConfig>, WithProject {
  public static final ConfigKey<String> RUNTIME_TYPE = new ConfigKey<>("RUNTIME_TYPE", String.class);
  public static final ConfigKey<String> RUNTIME_VERSION = new ConfigKey<>("RUNTIME_VERSION", String.class);


  private final Resources resources;
  private final Configurators configurators;

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
    if (Strings.isNullOrEmpty(resources.getName()) && !Strings.isNullOrEmpty(config.getName())) {
      resources.setName(config.getName());
    }

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

    if (config.getProject().getScmInfo() != null) {
      String uri = config.getProject().getScmInfo().getUri();
      String branch = config.getProject().getScmInfo().getBranch();
      String buildTypeType = config.getBuildType();
      Path modulePath = config.getProject().getScmInfo().getRoot().relativize(config.getProject().getRoot());

       resources.decorateCustom(ResourceGroup.NAME,
                                new AddBuildConfigToComponentDecorator(modulePath, uri, branch, buildTypeType));
    }

    if (config.isExposeService()) {
      resources.decorateCustom(ResourceGroup.NAME,new ExposeServiceDecorator());

      BaseConfig kubernetesConfig = getKubernetesConfig();
      Port[] ports = kubernetesConfig.getPorts();
      if (ports.length == 0) {
        throw new IllegalStateException("Ports need to be present on KubernetesConfig");
      }
      resources.decorateCustom(ResourceGroup.NAME,new AddExposedPortToComponentDecorator(ports[0].getContainerPort()));
    }

    if (type != null) {
      resources.decorateCustom(ResourceGroup.NAME,new AddRuntimeTypeToComponentDecorator(type)); // 
    }

    if (version != null) {
      resources.decorateCustom(ResourceGroup.NAME,new AddRuntimeVersionToComponentDecorator(version));
    }
    resources.decorateCustom(ResourceGroup.NAME,new DeploymentModeDecorator(config.getDeploymentMode()));
    for (Env env : config.getEnvs()) {
      resources.decorateCustom(ResourceGroup.NAME, new AddEnvToComponentDecorator(env));
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
   * @param config  The config.
   * @return The component.
   */
  private Component createComponent(ComponentConfig config) {
    Map<String, String> labels = resources.getLabels();
    labels.put(Labels.APP, config.getName());
    return new ComponentBuilder()
      .withNewMetadata()
      .withName(config.getName())
      .withLabels(labels)
      .endMetadata()
      .withNewSpec()
      .withDeploymentMode(config.getDeploymentMode())
      .withVersion(resources.getVersion())
      .endSpec()
      .build();
  }

  @Override
  public ConfigurationSupplier<ComponentConfig> getFallbackConfig() {
    Project p = getProject();
    return new ConfigurationSupplier<ComponentConfig>(new ComponentConfigBuilder()
                                                      .withName(p.getBuildInfo().getName())
                                                      .accept(new ApplyAutoBuild())
                                                      .accept(new ApplyProjectInfo(p)));
  }
}
