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
package io.ap4k.istio.generator;

import io.ap4k.WithSession;
import io.ap4k.Generator;
import io.ap4k.config.ConfigurationSupplier;
import io.ap4k.istio.adapter.IstioConfigAdapter;
import io.ap4k.istio.annotation.Istio;
import io.ap4k.istio.config.IstioConfig;
import io.ap4k.istio.handler.IstioHandler;

import javax.lang.model.element.Element;
import java.util.Map;

public interface IstioConfigGenerator extends Generator, WithSession {

  @Override
  default void add(Map map) {
    on(new ConfigurationSupplier<>(IstioConfigAdapter.newBuilder(propertiesMap(map, Istio.class))));
  }

  @Override
  default void add(Element element) {
    Istio istio = element.getAnnotation(Istio.class);
    on(istio != null
      ? new ConfigurationSupplier<>(IstioConfigAdapter.newBuilder(istio))
      : new ConfigurationSupplier<>(IstioConfig.newIstioConfigBuilder()));
  }

  default void on(ConfigurationSupplier<IstioConfig> config) {
    session.configurators().add(config);
    session.handlers().add(new IstioHandler(session.resources()));
  }
}
