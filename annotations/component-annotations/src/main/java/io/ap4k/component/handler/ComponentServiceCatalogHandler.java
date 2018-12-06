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
import io.ap4k.component.decorator.AddServiceInstanceToComponentDecorator;
import io.ap4k.servicecatalog.config.ServiceCatalogConfig;
import io.ap4k.servicecatalog.config.ServiceCatalogInstance;
import io.ap4k.kubernetes.config.Configuration;
import io.ap4k.servicecatalog.config.EditableServiceCatalogConfig;

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

  public void handle(ServiceCatalogConfig config) {
    for (ServiceCatalogInstance instance : config.getInstances()) {
      resources.decorateCustom(COMPONENT, new AddServiceInstanceToComponentDecorator(instance));
    }
  }

  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(ServiceCatalogConfig.class) ||
      type.equals(EditableServiceCatalogConfig.class);
  }
}
