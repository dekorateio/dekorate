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
package io.ap4k.jaeger.generator;

import io.ap4k.Generator;
import io.ap4k.WithSession;
import io.ap4k.config.ConfigurationSupplier;
import io.ap4k.jaeger.adapter.JaegerAgentConfigAdapter;
import io.ap4k.jaeger.annotation.EnableJaegerAgent;
import io.ap4k.jaeger.config.AddDefaultPortsToAgentConfigurator;
import io.ap4k.jaeger.config.JaegerAgentConfig;
import io.ap4k.jaeger.config.JaegerAgentConfigBuilder;
import io.ap4k.jaeger.handler.JaegerAgentHandler;

import javax.lang.model.element.Element;
import java.util.Map;

public interface JaegerAgentGenerator extends Generator, WithSession {

  @Override
  default void add(Map map) {
    on(new ConfigurationSupplier<>(JaegerAgentConfigAdapter.newBuilder(propertiesMap(map, EnableJaegerAgent.class))));
  }

  @Override
  default void add(Element element) {
    EnableJaegerAgent serviceMonitor = element.getAnnotation(EnableJaegerAgent.class);
    on(serviceMonitor != null
      ? new ConfigurationSupplier<>(JaegerAgentConfigAdapter.newBuilder(serviceMonitor))
      : new ConfigurationSupplier<>(new JaegerAgentConfigBuilder()));
  }

  default void on(ConfigurationSupplier<JaegerAgentConfig> config) {
    session.configurators().add(config);
    session.configurators().add(new AddDefaultPortsToAgentConfigurator());
    session.handlers().add(new JaegerAgentHandler(session.resources()));
  }
}
