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
package io.ap4k.servicecatalog.generator;

import io.ap4k.WithSession;
import io.ap4k.Generator;
import io.ap4k.config.ConfigurationSupplier;
import io.ap4k.servicecatalog.adapter.ServiceCatalogConfigAdapter;
import io.ap4k.servicecatalog.annotation.ServiceCatalog;
import io.ap4k.servicecatalog.config.ServiceCatalogConfig;
import io.ap4k.servicecatalog.config.ServiceCatalogConfigBuilder;
import io.ap4k.servicecatalog.handler.ServiceCatalogHandler;

import javax.lang.model.element.Element;
import java.util.Map;

public interface ServiceCatalogGenerator extends Generator, WithSession {

  @Override
  default void add(Map map) {
    on(new ConfigurationSupplier<>(ServiceCatalogConfigAdapter.newBuilder(propertiesMap(map, ServiceCatalog.class))));
  }

  @Override
  default void add(Element element) {
    ServiceCatalog serviceCatalog = element.getAnnotation(ServiceCatalog.class);
    on(serviceCatalog != null
      ? new ConfigurationSupplier<>(ServiceCatalogConfigAdapter.newBuilder(serviceCatalog))
      : new ConfigurationSupplier<>(new ServiceCatalogConfigBuilder()));
  }

  default void on(ConfigurationSupplier<ServiceCatalogConfig> config) {
    session.configurators().add(config);
    session.handlers().add(new ServiceCatalogHandler(session.resources()));
  }
}
