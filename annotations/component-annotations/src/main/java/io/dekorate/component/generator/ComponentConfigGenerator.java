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
package io.dekorate.component.generator;

import io.dekorate.Generator;
import io.dekorate.WithProject;
import io.dekorate.component.adapter.ComponentConfigAdapter;
import io.dekorate.component.annotation.ComponentApplication;
import io.dekorate.component.config.ComponentConfig;
import io.dekorate.component.configurator.ApplyProject;
import io.dekorate.component.handler.ComponentHandler;
import io.dekorate.component.handler.ComponentServiceCatalogHandler;
import io.dekorate.config.ConfigurationSupplier;

import javax.lang.model.element.Element;
import java.util.Map;

public interface ComponentConfigGenerator extends Generator, WithProject {

  String GENERATOR_KEY = "component";

  @Override
  default void add(Map map) {
    add(new ConfigurationSupplier<>(
      ComponentConfigAdapter
        .newBuilder(propertiesMap(map, ComponentApplication.class))
        .accept(new ApplyProject(getProject()))));
  }

  @Override
  default void add(Element element) {
    ComponentApplication component = element.getAnnotation(ComponentApplication.class);
    add(component != null
       ? new ConfigurationSupplier<>(ComponentConfigAdapter.newBuilder(component).accept(new ApplyProject(getProject())))
      : new ConfigurationSupplier<>(ComponentConfig.newComponentConfigBuilder().accept(new ApplyProject(getProject()))));
  }

  default void add(ConfigurationSupplier<ComponentConfig> config) {
    session.configurators().add(config);
    session.handlers().add(new ComponentHandler(session.resources(), session.configurators()));
    session.handlers().add(new ComponentServiceCatalogHandler(session.resources()));
  }
}
