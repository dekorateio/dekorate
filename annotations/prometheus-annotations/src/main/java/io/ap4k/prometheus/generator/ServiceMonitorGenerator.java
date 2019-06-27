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
package io.ap4k.prometheus.generator;

import io.ap4k.Generator;
import io.ap4k.WithSession;
import io.ap4k.config.ConfigurationSupplier;
import io.ap4k.prometheus.adapter.ServiceMonitorConfigAdapter;
import io.ap4k.prometheus.annotation.EnableServiceMonitor;
import io.ap4k.prometheus.config.ServiceMonitorConfig;
import io.ap4k.prometheus.config.ServiceMonitorConfigBuilder;
import io.ap4k.prometheus.handler.ServiceMonitorHandler;

import javax.lang.model.element.Element;
import java.util.Map;

public interface ServiceMonitorGenerator extends Generator, WithSession {

  @Override
  default void add(Map map) {
    on(new ConfigurationSupplier<>(ServiceMonitorConfigAdapter.newBuilder(propertiesMap(map, EnableServiceMonitor.class))));
  }

  @Override
  default void add(Element element) {
    EnableServiceMonitor serviceMonitor = element.getAnnotation(EnableServiceMonitor.class);
    on(serviceMonitor != null
      ? new ConfigurationSupplier<>(ServiceMonitorConfigAdapter.newBuilder(serviceMonitor))
      : new ConfigurationSupplier<>(new ServiceMonitorConfigBuilder()));
  }

  default void on(ConfigurationSupplier<ServiceMonitorConfig> config) {
    session.configurators().add(config);
    session.handlers().add(new ServiceMonitorHandler(session.resources()));
  }
}
