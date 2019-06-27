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
package io.ap4k.component.generator;

import io.ap4k.Generator;
import io.ap4k.WithProject;
import io.ap4k.component.adapter.CompositeConfigAdapter;
import io.ap4k.component.annotation.CompositeApplication;
import io.ap4k.component.config.CompositeConfig;
import io.ap4k.component.configurator.ApplyProject;
import io.ap4k.component.handler.ComponentHandler;
import io.ap4k.component.handler.ComponentServiceCatalogHandler;
import io.ap4k.config.ConfigurationSupplier;

import javax.lang.model.element.Element;
import java.util.Map;

public interface CompositeConfigGenerator extends Generator, WithProject {
  String GENERATOR_KEY = "composite";
  
  @Override
  default void add(Map map) {
    add(new ConfigurationSupplier<>(
      CompositeConfigAdapter
        .newBuilder(propertiesMap(map, CompositeApplication.class))
        .accept(new ApplyProject(getProject()))));
  }

  @Override
  default void add(Element element) {
    CompositeApplication composite = element.getAnnotation(CompositeApplication.class);
    add(composite != null
       ? new ConfigurationSupplier<>(CompositeConfigAdapter.newBuilder(composite).accept(new ApplyProject(getProject())))
      : new ConfigurationSupplier<>(CompositeConfig.newCompositeConfigBuilder().accept(new ApplyProject(getProject()))));
  }

  default void add(ConfigurationSupplier<CompositeConfig> config) {
    session.configurators().add(config);
    session.handlers().add(new ComponentHandler(session.resources()));
    session.handlers().add(new ComponentServiceCatalogHandler(session.resources()));
  }
}
