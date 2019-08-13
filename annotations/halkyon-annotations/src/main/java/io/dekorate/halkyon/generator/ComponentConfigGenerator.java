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
package io.dekorate.halkyon.generator;

import java.util.Map;

import javax.lang.model.element.Element;

import io.dekorate.Generator;
import io.dekorate.Session;
import io.dekorate.WithProject;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.halkyon.adapter.ComponentConfigAdapter;
import io.dekorate.halkyon.annotation.HalkyonComponent;
import io.dekorate.halkyon.config.ComponentConfig;
import io.dekorate.halkyon.configurator.ApplyProject;
import io.dekorate.halkyon.handler.ComponentHandler;
import io.dekorate.halkyon.handler.ComponentServiceCatalogHandler;

public interface ComponentConfigGenerator extends Generator, WithProject {
  
  String GENERATOR_KEY = "component";
  
  @Override
  default void add(Map map) {
    add(new ConfigurationSupplier<>(
      ComponentConfigAdapter
        .newBuilder(propertiesMap(map, HalkyonComponent.class))
        .accept(new ApplyProject(getProject()))));
  }
  
  @Override
  default void add(Element element) {
    HalkyonComponent component = element.getAnnotation(HalkyonComponent.class);
    add(component != null
      ? new ConfigurationSupplier<>(ComponentConfigAdapter.newBuilder(component).accept(new ApplyProject(getProject())))
      : new ConfigurationSupplier<>(ComponentConfig.newComponentConfigBuilder().accept(new ApplyProject(getProject()))));
  }
  
  default void add(ConfigurationSupplier<ComponentConfig> config) {
    Session session = getSession();
    session.configurators().add(config);
    session.handlers().add(new ComponentHandler(session.resources(), session.configurators()));
    session.handlers().add(new ComponentServiceCatalogHandler(session.resources()));
  }
}
