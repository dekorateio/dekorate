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
package io.ap4k.component.decorator;

import io.ap4k.component.model.ComponentSpecBuilder;
import io.ap4k.kubernetes.decorator.Decorator;
import io.ap4k.servicecatalog.config.Parameter;
import io.ap4k.servicecatalog.config.ServiceCatalogInstance;
import io.ap4k.doc.Description;

import java.util.Arrays;

@Description("Add the service instance information to the component.")
public class AddServiceInstanceToComponent extends Decorator<ComponentSpecBuilder> {

  private final ServiceCatalogInstance instance;

  public AddServiceInstanceToComponent(ServiceCatalogInstance instance) {
    this.instance = instance;
  }

  @Override
  public void visit(ComponentSpecBuilder component) {
    if (hasService(component)) {
      return;
    }

    component.addNewService()
      .withName(instance.getName())
      .withServiceClass(instance.getServiceClass())
      .withServicePlan(instance.getServicePlan())
      .withSecretName(instance.getBindingSecret())
      .withParameters(convertToModelParameter(instance.getParameters()))
      .endService();
  }

  private boolean hasService(ComponentSpecBuilder componentSpec) {
    return Arrays.asList(componentSpec.getService()).stream().filter(s -> s.getName().equals(instance.getName())).count() > 0;
  }

  private io.ap4k.component.model.Parameter[] convertToModelParameter(Parameter[] parametersConfig) {
    io.ap4k.component.model.Parameter[] parameters = new io.ap4k.component.model.Parameter[parametersConfig.length];
    io.ap4k.component.model.Parameter parameter;

    for( int i = 0; i < parametersConfig.length; i++) {
      Parameter paramConfig = parametersConfig[i];

      parameter = new io.ap4k.component.model.Parameter();
      parameter.setName(paramConfig.getKey());
      parameter.setValue(paramConfig.getValue());
      parameters[i] = parameter;
    }
    return parameters;
  }
}
