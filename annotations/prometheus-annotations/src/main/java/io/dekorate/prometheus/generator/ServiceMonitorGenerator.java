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
package io.dekorate.prometheus.generator;

import io.dekorate.Generator;
import io.dekorate.Session;
import io.dekorate.WithSession;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.prometheus.adapter.ServiceMonitorConfigAdapter;
import io.dekorate.prometheus.annotation.EnableServiceMonitor;
import io.dekorate.prometheus.config.ServiceMonitorConfig;
import io.dekorate.prometheus.config.ServiceMonitorConfigBuilder;
import io.dekorate.prometheus.handler.ServiceMonitorHandler;

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
    Session session = getSession();
    session.configurators().add(config);
    session.handlers().add(new ServiceMonitorHandler(session.resources()));
  }
}
