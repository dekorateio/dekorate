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
package io.dekorate.servicecatalog.generator;

import java.util.Map;

import io.dekorate.Generator;
import io.dekorate.Session;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.config.PropertyConfiguration;
import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.servicecatalog.adapter.ServiceCatalogConfigAdapter;
import io.dekorate.servicecatalog.config.ServiceCatalogConfig;
import io.dekorate.servicecatalog.handler.ServiceCatalogHandler;

public interface ServiceCatalogGenerator extends Generator  {

  default String getKey() {
    return "svcat";
  }

  default Class<? extends Configuration> getConfigType() {
    return ServiceCatalogConfig.class;
  }

  @Override
  default void addAnnotationConfiguration(Map map) {
    on(new AnnotationConfiguration<>(ServiceCatalogConfigAdapter.newBuilder(propertiesMap(map, ServiceCatalogConfig.class))));
  }

  @Override
  default void addPropertyConfiguration(Map map) {
    on(new PropertyConfiguration<>(ServiceCatalogConfigAdapter.newBuilder(propertiesMap(map, ServiceCatalogConfig.class))));
  }

  default void on(ConfigurationSupplier<ServiceCatalogConfig> config) {
    Session session = getSession();
    session.configurators().add(config);
    session.handlers().add(new ServiceCatalogHandler(session.resources()));
  }
}
