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
package io.dekorate.servicecatalog.handler;

import java.util.Arrays;

import io.dekorate.Handler;
import io.dekorate.Resources;
import io.dekorate.doc.Description;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.EnvBuilder;
import io.dekorate.kubernetes.decorator.AddEnvVarDecorator;
import io.dekorate.servicecatalog.config.EditableServiceCatalogConfig;
import io.dekorate.servicecatalog.config.ServiceCatalogConfig;
import io.dekorate.servicecatalog.decorator.AddServiceBindingResourceDecorator;
import io.dekorate.servicecatalog.decorator.AddServiceInstanceResourceDecorator;

@Description("Adds service instance and binding and inject binding info to container environment.")
public class ServiceCatalogHandler implements Handler<ServiceCatalogConfig> {

  private final Resources resources;

  // only used for testing
  ServiceCatalogHandler() {
    this(new Resources());
  }

  public ServiceCatalogHandler(Resources resources) {
    this.resources = resources;
  }

  @Override
  public int order() {
    return 410;
  }

  @Override
  public String getKey() {
    return "svcat";
  }

  @Override
  public void handle(ServiceCatalogConfig config) {
    Arrays.stream(config.getInstances()).forEach(i -> { 
        resources.decorate(new AddServiceInstanceResourceDecorator(i));
        if (i.getBindingSecret() != null) {
          resources.decorate(new AddServiceBindingResourceDecorator(i));
          resources.decorate(new AddEnvVarDecorator(new EnvBuilder().withSecret(i.getBindingSecret()).build()));
        }
      });
  }

  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(ServiceCatalogConfig.class) ||
      type.equals(EditableServiceCatalogConfig.class);
  }

}
