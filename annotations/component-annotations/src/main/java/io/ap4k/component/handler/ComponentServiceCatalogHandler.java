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
import io.ap4k.component.model.Capability;
import io.ap4k.component.model.CapabilityBuilder;
import io.ap4k.kubernetes.config.Configuration;
import io.ap4k.servicecatalog.config.EditableServiceCatalogConfig;
import io.ap4k.servicecatalog.config.Parameter;
import io.ap4k.servicecatalog.config.ServiceCatalogConfig;
import io.ap4k.servicecatalog.config.ServiceCatalogInstance;

public class ComponentServiceCatalogHandler implements Handler<ServiceCatalogConfig> {

  private static final String COMPONENT = "component";

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
      resources.addCustom(COMPONENT, createServiceInstance(instance));
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

  private io.ap4k.component.model.Parameter[] convertToModelParameter(Parameter[] parametersConfig) {
    io.ap4k.component.model.Parameter[] parameters = new io.ap4k.component.model.Parameter[parametersConfig.length];
    io.ap4k.component.model.Parameter parameter;

    for (int i = 0; i < parametersConfig.length; i++) {
      Parameter paramConfig = parametersConfig[i];

      parameter = new io.ap4k.component.model.Parameter();
      parameter.setName(paramConfig.getKey());
      parameter.setValue(paramConfig.getValue());
      parameters[i] = parameter;
    }
    return parameters;
  }

}
