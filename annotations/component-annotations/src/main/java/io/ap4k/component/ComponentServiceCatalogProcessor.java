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

import io.ap4k.Processor;
import io.ap4k.Resources;
import io.ap4k.component.decorator.AddServiceInstanceToComponent;
import io.ap4k.servicecatalog.config.ServiceCatalogConfig;
import io.ap4k.servicecatalog.config.ServiceCatalogInstance;
import io.ap4k.config.Configuration;
import io.ap4k.servicecatalog.config.EditableServiceCatalogConfig;

public class ComponentServiceCatalogProcessor implements Processor<ServiceCatalogConfig> {

  private static final String COMPONENT = "component";

  private final Resources resources;

  public ComponentServiceCatalogProcessor() {
    this(new Resources());
  }
  public ComponentServiceCatalogProcessor(Resources resources) {
    this.resources = resources;
  }

  public void process(ServiceCatalogConfig config) {
    for (ServiceCatalogInstance instance : config.getInstances()) {
      resources.accept(COMPONENT, new AddServiceInstanceToComponent(instance));
    }
  }

  public boolean accepts(Class<? extends Configuration> type) {
    return type.equals(ServiceCatalogConfig.class) ||
      type.equals(EditableServiceCatalogConfig.class);
  }
}
