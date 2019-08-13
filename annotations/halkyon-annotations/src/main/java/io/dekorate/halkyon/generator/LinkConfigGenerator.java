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
import io.dekorate.halkyon.adapter.LinkConfigAdapter;
import io.dekorate.halkyon.annotation.Link;
import io.dekorate.halkyon.config.LinkConfig;
import io.dekorate.halkyon.handler.LinkHandler;

public interface LinkConfigGenerator extends Generator, WithProject {
  String GENERATOR_KEY = "link";
  
  @Override
  default void add(Map map) {
    add(new ConfigurationSupplier<>(LinkConfigAdapter.newBuilder(propertiesMap(map, Link.class))));
  }
  
  @Override
  default void add(Element element) {
    Link link = element.getAnnotation(Link.class);
    add(link != null
      ? new ConfigurationSupplier<>(LinkConfigAdapter.newBuilder(link))
      : new ConfigurationSupplier<>(LinkConfig.newLinkConfigBuilder()));
  }
  
  default void add(ConfigurationSupplier<LinkConfig> config) {
    Session session = getSession();
    session.configurators().add(config);
    session.handlers().add(new LinkHandler(session.resources()));
  }
}
