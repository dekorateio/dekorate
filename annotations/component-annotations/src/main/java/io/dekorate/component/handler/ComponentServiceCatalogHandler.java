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

import io.dekorate.Handler;
import io.dekorate.Resources;
import io.dekorate.component.model.Capability;
import io.dekorate.component.model.CapabilityBuilder;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.servicecatalog.config.EditableServiceCatalogConfig;
import io.dekorate.servicecatalog.config.Parameter;
import io.dekorate.servicecatalog.config.ServiceCatalogConfig;
import io.dekorate.servicecatalog.config.ServiceCatalogInstance;

public class ComponentServiceCatalogHandler implements Handler<ServiceCatalogConfig> {
  private final Resources resources;

  // only used for testing
  ComponentServiceCatalogHandler() {
    this(new Resources());
  }

  public ComponentServiceCatalogHandler(Resources resources) {
    this.resources = resources;
  }

  @Override
  public int order() {
    return 650;
  }

  public void handle(ServiceCatalogConfig config) {
    for (ServiceCatalogInstance instance : config.getInstances()) {
      resources.addCustom(ResourceGroup.NAME, createServiceInstance(instance));
    }
  }

  private Capability createServiceInstance(ServiceCatalogInstance instance) {
    return new CapabilityBuilder()
      .withNewMetadata()
      .withName(instance.getName())
      .endMetadata()
      .withNewSpec()
      .withServiceClass(instance.getServiceClass())
      .withServicePlan(instance.getServicePlan())
      .withSecretName(instance.getBindingSecret())
      .withParameters(convertToModelParameter(instance.getParameters()))
      .endSpec().build();
  }

  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(ServiceCatalogConfig.class) ||
      type.equals(EditableServiceCatalogConfig.class);
  }

  private io.dekorate.component.model.Parameter[] convertToModelParameter(Parameter[] parametersConfig) {
    io.dekorate.component.model.Parameter[] parameters = new io.dekorate.component.model.Parameter[parametersConfig.length];
    io.dekorate.component.model.Parameter parameter;

    for (int i = 0; i < parametersConfig.length; i++) {
      Parameter paramConfig = parametersConfig[i];

      parameter = new io.dekorate.component.model.Parameter();
      parameter.setName(paramConfig.getKey());
      parameter.setValue(paramConfig.getValue());
      parameters[i] = parameter;
    }
    return parameters;
  }

}
