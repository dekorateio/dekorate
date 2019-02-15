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
package io.ap4k.application.generator;

import io.ap4k.Generator;
import io.ap4k.WithSession;
import io.ap4k.application.adapter.ApplicationConfigAdapter;
import io.ap4k.application.annotation.ApplicationInfo;
import io.ap4k.application.config.ApplicationConfigBuilder;
import io.ap4k.application.handler.ApplicationHandler;
import io.ap4k.config.ConfigurationSupplier;

import javax.lang.model.element.Element;
import java.util.Map;

public interface ApplicationResourceGenerator extends Generator, WithSession {

  @Override
  default void add(Map map) {
  }

  @Override
  default void add(Element element) {
    ApplicationInfo info = element.getAnnotation(ApplicationInfo.class);
    ApplicationConfigBuilder builder = ApplicationConfigAdapter.newBuilder(info);
    add(new ConfigurationSupplier<>(builder));
  }

  default void add(ConfigurationSupplier<?> config) {
    session.configurators().add(config);
    session.handlers().add(new ApplicationHandler(session.resources()));
  }
}
