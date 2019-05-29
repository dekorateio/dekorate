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
package io.ap4k.component.generator;

import io.ap4k.Generator;
import io.ap4k.WithProject;
import io.ap4k.component.adapter.LinkConfigAdapter;
import io.ap4k.component.annotation.Link;
import io.ap4k.component.config.LinkConfig;
import io.ap4k.component.configurator.ApplyProject;
import io.ap4k.component.handler.ComponentHandler;
import io.ap4k.component.handler.ComponentServiceCatalogHandler;
import io.ap4k.component.handler.LinkHandler;
import io.ap4k.config.ConfigurationSupplier;

import javax.lang.model.element.Element;
import java.util.Map;

public interface LinkConfigGenerator extends Generator, WithProject {

//  @Override
//  default void add(Map map) {
//    on(new ConfigurationSupplier<>(LinkConfigAdapter.newBuilder(propertiesMap(map, Link.class)).accept(new ApplyProject(getProject()))));
//  }
//
//  @Override
//  default void add(Element element) {
//    Link link = element.getAnnotation(Link.class);
//    on(link != null
//      ? new ConfigurationSupplier<>(LinkConfigAdapter.newBuilder(link).accept(new ApplyProject(getProject())))
//      : new ConfigurationSupplier<>(LinkConfig.newLinkConfigBuilder().accept(new ApplyProject(getProject()))));
//  }

  default void on(ConfigurationSupplier<LinkConfig> config) {
    session.configurators().add(config);
    session.handlers().add(new LinkHandler(session.resources()));
  }
}
