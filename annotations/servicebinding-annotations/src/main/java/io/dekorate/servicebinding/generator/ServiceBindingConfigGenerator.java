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
 */
package io.dekorate.servicebinding.generator;

import java.util.Map;

import io.dekorate.Generator;
import io.dekorate.Session;
import io.dekorate.WithProject;
import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.config.PropertyConfiguration;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.servicebinding.adapter.ServiceBindingConfigAdapter;
import io.dekorate.servicebinding.config.ServiceBindingConfig;
import io.dekorate.servicebinding.configurator.ApplyProjectInfo;
import io.dekorate.servicebinding.handler.ServiceBindingHandler;

public interface ServiceBindingConfigGenerator extends Generator, WithProject {

  String SERVICEBINDING = "servicebinding";

  default String getKey() {
    return SERVICEBINDING;
  }

  default Class<? extends Configuration> getConfigType() {
    return ServiceBindingConfig.class;
  }

  @Override
  default void addAnnotationConfiguration(Map map) {
    add(new AnnotationConfiguration<>(ServiceBindingConfigAdapter
        .newBuilder(propertiesMap(map, ServiceBindingConfig.class)).accept(new ApplyProjectInfo(getProject()))));
  }

  @Override
  default void addPropertyConfiguration(Map map) {
    add(new PropertyConfiguration<>(ServiceBindingConfigAdapter
        .newBuilder(propertiesMap(map, ServiceBindingConfig.class)).accept(new ApplyProjectInfo(getProject()))));
  }

  default void add(ConfigurationSupplier<ServiceBindingConfig> config) {
    Session session = getSession();
    session.configurators().add(config);
    session.handlers().add(new ServiceBindingHandler(session.resources(), session.configurators()));
  }
}
