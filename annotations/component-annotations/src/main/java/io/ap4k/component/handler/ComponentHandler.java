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
 * 
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
import io.ap4k.component.model.DeploymentMode;
import io.ap4k.kubernetes.config.ConfigKey;
import io.ap4k.kubernetes.config.Configuration;
import io.ap4k.kubernetes.config.Env;
import io.ap4k.utils.Strings;

public class ComponentHandler implements Handler<CompositeConfig> {

  private static final String COMPONENT = "component";
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
    resources.addCustom(COMPONENT, createComponent(config));
    addVisitors(config);
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
      resources.decorateCustom(COMPONENT,new AddRuntimeTypeToComponentDecorator(type));
    }

    if (version != null) {
      resources.decorateCustom(COMPONENT,new AddRuntimeVersionToComponentDecorator(version));
    }
    for (Env env : config.getEnvVars()) {
      resources.decorateCustom(COMPONENT, new AddEnvToComponentDecorator(env));
    }
//    for (Link link : config.getLinks()) {
//      resources.decorateCustom(COMPONENT, new AddLinkToComponentDecorator(link));
//    }
  }

  /**
   * Create a {@link Component} from a {@link CompositeConfig}.
   * @param config  The config.
   * @return        The component.
   */
  private Component createComponent(CompositeConfig config) {
    return new ComponentBuilder()
      .withNewMetadata()
      .withName(resources.getName())
      .withLabels(resources.getLabels())
      .endMetadata()
      .withNewSpec()
      .withDeploymentMode(DeploymentMode.dev)
      .withExposeService(config.isExposeService())
      .withVersion(resources.getVersion())
      .endSpec()
      .build();
  }
}
