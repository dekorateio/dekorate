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
package io.ap4k.component;

import io.ap4k.Handler;
import io.ap4k.Resources;
import io.ap4k.component.config.CompositeConfig;
import io.ap4k.component.config.EditableCompositeConfig;
import io.ap4k.component.decorator.AddEnvToComponent;
import io.ap4k.component.decorator.AddRuntimeToComponent;
import io.ap4k.config.ConfigKey;
import io.ap4k.config.Configuration;
import io.ap4k.config.Env;
import io.ap4k.config.KubernetesConfig;

public class ComponentHandler implements Handler<CompositeConfig> {

  private static final String COMPONENT = "component";
  public static final ConfigKey<String> RUNTIME_TYPE = new ConfigKey<>("RUNTIME_TYPE", String.class);

  private final Resources resources;

  public ComponentHandler() {
    this(new Resources());
  }
  public ComponentHandler(Resources resources) {
    this.resources = resources;
  }


  @Override
  public void handle(CompositeConfig config) {
    addVisitors(config);
  }

  @Override
  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(CompositeConfig.class) ||
      type.equals(EditableCompositeConfig.class);
  }

  private void addVisitors(KubernetesConfig config) {
    String type = config.getAttribute(RUNTIME_TYPE);
    if (type != null) {
      resources.decorateCustom(COMPONENT,new AddRuntimeToComponent(type));
    }
    for (Env env : config.getEnvVars()) {
      resources.decorateCustom(COMPONENT, new AddEnvToComponent(env));
    }
  }

}
