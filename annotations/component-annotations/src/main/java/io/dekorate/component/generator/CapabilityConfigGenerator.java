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

package io.dekorate.component.generator;

import io.dekorate.Generator;
import io.dekorate.WithProject;
import io.dekorate.component.adapter.CapabilityConfigAdapter;
import io.dekorate.component.annotation.Capability;
import io.dekorate.component.config.CapabilityConfig;
import io.dekorate.component.handler.CapabilityHandler;
import io.dekorate.config.ConfigurationSupplier;

import javax.lang.model.element.Element;
import java.util.Map;

public interface CapabilityConfigGenerator extends Generator, WithProject {

  String GENERATOR_KEY = "capability";

  @Override
  default void add(Map map) {
    add(new ConfigurationSupplier<>(
      CapabilityConfigAdapter
      .newBuilder(propertiesMap(map, Capability.class))));
  }

  @Override
  default void add(Element element) {
    Capability capability = element.getAnnotation(Capability.class);
    add(capability != null
        ? new ConfigurationSupplier<>(CapabilityConfigAdapter.newBuilder(capability))
        : new ConfigurationSupplier<>(CapabilityConfig.newCapabilityConfigBuilder()));
  }

  default void add(ConfigurationSupplier<CapabilityConfig> config) {
    session.configurators().add(config);
    session.handlers().add(new CapabilityHandler(session.resources()));
  }
}
