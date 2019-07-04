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
package io.ap4k.component.handler;

import io.ap4k.Handler;
import io.ap4k.Resources;
import io.ap4k.component.config.ComponentConfig;
import io.ap4k.component.config.EditableComponentConfig;
import io.ap4k.component.decorator.AddBuildConfigToComponentDecorator;
import io.ap4k.component.decorator.AddEnvToComponentDecorator;
import io.ap4k.component.decorator.AddRuntimeTypeToComponentDecorator;
import io.ap4k.component.decorator.AddRuntimeVersionToComponentDecorator;
import io.ap4k.component.model.Component;
import io.ap4k.component.model.ComponentBuilder;
import io.ap4k.kubernetes.config.ConfigKey;
import io.ap4k.kubernetes.config.Configuration;
import io.ap4k.kubernetes.config.Env;
import io.ap4k.utils.Strings;

import java.io.IOException;

public class ComponentHandler implements Handler<ComponentConfig> {
  public static final ConfigKey<String> RUNTIME_TYPE = new ConfigKey<>("RUNTIME_TYPE", String.class);
  public static final ConfigKey<String> RUNTIME_VERSION = new ConfigKey<>("RUNTIME_VERSION", String.class);
//  public static final String GITHUB_SSH = "git@github.com:";
//  public static final String GITHUB_HTTPS = "https://github.com/";


  private final Resources resources;

  // only used for testing
  public ComponentHandler() {
    this(new Resources());
  }

  public ComponentHandler(Resources resources) {
    this.resources = resources;
  }

  @Override
  public int order() {
    return 1100;
  }

  @Override
  public void handle(ComponentConfig config) {
    if (Strings.isNullOrEmpty(resources.getName())) {
      resources.setName(config.getName());
    }
    resources.addCustom(ResourceGroup.NAME, createComponent(config));
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

    String uri = config.getProject().getScmInfo().getUri();
    String branch = config.getProject().getScmInfo().getBranch();
    String name = config.getProject().getBuildInfo().getName();
    String buildConfigType = config.getBuildconfig().getType();

    resources.decorateCustom(ResourceGroup.NAME,new AddBuildConfigToComponentDecorator(uri, branch, name, buildConfigType));

    if (type != null) {
      resources.decorateCustom(ResourceGroup.NAME,new AddRuntimeTypeToComponentDecorator(type));
    }

    if (version != null) {
      resources.decorateCustom(ResourceGroup.NAME,new AddRuntimeVersionToComponentDecorator(version));
    }
    for (Env env : config.getEnvs()) {
      resources.decorateCustom(ResourceGroup.NAME, new AddEnvToComponentDecorator(env));
    }
  }

  /**
   * Create a {@link Component} from a {@link ComponentConfig}.
   * @param config  The config.
   * @return The component.
   */
  private Component createComponent(ComponentConfig config) {
       return new ComponentBuilder()
      .withNewMetadata()
      .withName(resources.getName())
      .withLabels(resources.getLabels())
      .endMetadata()
      .withNewSpec()
      .withDeploymentMode(config.getDeploymentMode())
      .withExposeService(config.isExposeService())
      .withVersion(resources.getVersion())
      .endSpec()
      .build();
  }
}
