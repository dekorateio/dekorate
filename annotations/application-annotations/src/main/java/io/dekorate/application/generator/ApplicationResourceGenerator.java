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
package io.dekorate.application.generator;

import io.dekorate.Generator;
import io.dekorate.Session;
import io.dekorate.WithSession;
import io.dekorate.application.adapter.ApplicationConfigAdapter;
import io.dekorate.application.annotation.EnableApplicationResource;
import io.dekorate.application.config.ApplicationConfigBuilder;
import io.dekorate.application.handler.ApplicationHandler;
import io.dekorate.config.ConfigurationSupplier;

import javax.lang.model.element.Element;
import java.util.Map;

public interface ApplicationResourceGenerator extends Generator, WithSession {

  @Override
  default void add(Map map) {
  }

  @Override
  default void add(Element element) {
    EnableApplicationResource info = element.getAnnotation(EnableApplicationResource.class);
    ApplicationConfigBuilder builder = ApplicationConfigAdapter.newBuilder(info);
    add(new ConfigurationSupplier<>(builder));
  }

  default void add(ConfigurationSupplier<?> config) {
    Session session = getSession();
    session.configurators().add(config);
    session.handlers().add(new ApplicationHandler(session.resources()));
  }
}
