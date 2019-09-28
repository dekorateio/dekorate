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

package io.dekorate.s2i.generator;

import java.util.Map;

import javax.lang.model.element.Element;

import io.dekorate.Generator;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.Session;
import io.dekorate.WithSession;
import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.config.PropertyConfiguration;
import io.dekorate.s2i.adapter.*;
import io.dekorate.s2i.annotation.S2iBuild;
import io.dekorate.s2i.config.*;
import io.dekorate.s2i.handler.S2iHanlder;

public interface S2iBuildGenerator extends Generator, WithSession {

  @Override
  default void add(Map map) {
    on(new PropertyConfiguration<S2iBuildConfig>(S2iBuildConfigAdapter.newBuilder(propertiesMap(map, S2iBuild.class))));
  }

  @Override
  default void add(Element element) {
    S2iBuild enableS2iBuild = element.getAnnotation(S2iBuild.class);
    on(enableS2iBuild != null
      ? new AnnotationConfiguration<S2iBuildConfig>(S2iBuildConfigAdapter.newBuilder(enableS2iBuild))
      : new AnnotationConfiguration<S2iBuildConfig>(new S2iBuildConfigBuilder()));
  }

  default void on(ConfigurationSupplier<S2iBuildConfig> config) {
    Logger log = LoggerFactory.getLogger();

    log.info("Registering s2i handler!");
    Session session = getSession();
    session.configurators().add(config);
    session.handlers().add(new S2iHanlder(session.resources()));
  }
}
