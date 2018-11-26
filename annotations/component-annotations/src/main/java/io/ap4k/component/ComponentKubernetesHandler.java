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
import io.ap4k.component.model.Component;
import io.ap4k.component.model.ComponentBuilder;
import io.ap4k.component.model.DeploymentType;
import io.ap4k.component.decorator.AddEnvToComponent;
import io.ap4k.config.Configuration;
import io.ap4k.config.Env;
import io.ap4k.config.KubernetesConfig;
import io.ap4k.config.EditableKubernetesConfig;

public class ComponentKubernetesHandler implements Handler<KubernetesConfig> {

  private static final String COMPONENT = "component";

  private final Resources resources;

  public ComponentKubernetesHandler() {
    this(new Resources());
  }
  public ComponentKubernetesHandler(Resources resources) {
    this.resources = resources;
  }


  @Override
  public void handle(KubernetesConfig config) {
    resources.addExplicit(COMPONENT, createComponent(config));
    addVisitors(config);
  }

  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(KubernetesConfig.class) ||
      type.equals(EditableKubernetesConfig.class);
  }

  /**
   * Create a {@link Component} from a {@link KubernetesConfig}.
   * @param config  The config.
   * @return        The component.
   */
  private Component createComponent(KubernetesConfig config) {
    return new ComponentBuilder()
      .withNewMetadata()
      .withName(config.getName())
      .endMetadata()
      .withNewSpec()
      .withDeploymentMode(DeploymentType.innerloop)
      .endSpec()
      .build();
  }


  private void addVisitors(KubernetesConfig config) {
    for (Env env : config.getEnvVars()) {
      resources.acceptExplicit(COMPONENT, new AddEnvToComponent(env));
    }
  }
}
