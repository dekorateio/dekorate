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
 **/
package io.ap4k.component.handler;

import io.ap4k.Handler;
import io.ap4k.Resources;
import io.ap4k.component.config.CompositeConfig;
import io.ap4k.component.config.EditableCompositeConfig;
import io.ap4k.component.decorator.AddEnvToComponentDecorator;
import io.ap4k.component.decorator.AddRuntimeTypeToComponentDecorator;
import io.ap4k.component.decorator.AddRuntimeVersionToComponentDecorator;
import io.ap4k.component.model.Component;
import io.ap4k.component.model.ComponentBuilder;
import io.ap4k.component.model.ComponentFluent;
import io.ap4k.kubernetes.config.ConfigKey;
import io.ap4k.kubernetes.config.Configuration;
import io.ap4k.kubernetes.config.Env;
import io.ap4k.utils.Strings;

public class ComponentHandler implements Handler<CompositeConfig> {
  public static final ConfigKey<String> RUNTIME_TYPE = new ConfigKey<>("RUNTIME_TYPE", String.class);
  public static final ConfigKey<String> RUNTIME_VERSION = new ConfigKey<>("RUNTIME_VERSION", String.class);

  private final Resources resources;

  public ComponentHandler(Resources resources) {
    this.resources = resources;
  }

  @Override
  public int order() {
    return 1100;
  }

  @Override
  public void handle(CompositeConfig config) {
    if (Strings.isNullOrEmpty(resources.getName())) {
      resources.setName(config.getName());
    }
    resources.addCustom(ResourceGroup.NAME, createComponent(config));
  }

  @Override
  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(CompositeConfig.class) ||
      type.equals(EditableCompositeConfig.class);
  }

  private void addVisitors(CompositeConfig config) {
    String type = config.getAttribute(RUNTIME_TYPE);
    String version = config.getAttribute(RUNTIME_VERSION);

    if (type != null) {
      resources.decorateCustom(ResourceGroup.NAME,new AddRuntimeTypeToComponentDecorator(type));
    }

    if (version != null) {
      resources.decorateCustom(ResourceGroup.NAME,new AddRuntimeVersionToComponentDecorator(version));
    }
    for (Env env : config.getEnvVars()) {
      resources.decorateCustom(ResourceGroup.NAME, new AddEnvToComponentDecorator(env));
    }
  }

  /**
   * Create a {@link Component} from a {@link CompositeConfig}.
   * @param config  The config.
   * @return The component.
   */
  private Component createComponent(CompositeConfig config) {
    final ComponentFluent.SpecNested<ComponentBuilder> specBuilder = new ComponentBuilder()
      .withNewMetadata()
      .withName(resources.getName())
      .withLabels(resources.getLabels())
      .endMetadata()
      .withNewSpec();

    final String type = config.getAttribute(RUNTIME_TYPE);
    if (type != null) {
      specBuilder.withRuntime(type);
    }

    final String version = config.getAttribute(RUNTIME_VERSION);
    if (version != null) {
      specBuilder.withVersion(version);
    } else {
      specBuilder.withVersion(resources.getVersion());
    }

    for (Env env : config.getEnvVars()) {
      specBuilder.addNewEnv(env.getName(), env.getValue());
    }

    return specBuilder
      .withDeploymentMode(config.getDeploymentMode())
      .withExposeService(config.isExposeService())
      .endSpec()
      .build();
  }
}
