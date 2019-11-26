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
package io.dekorate.option.generator;

import java.util.Map;

import javax.lang.model.element.Element;

import io.dekorate.Generator;
import io.dekorate.Session;
import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.config.PropertyConfiguration;
import io.dekorate.option.adapter.JvmConfigAdapter;
import io.dekorate.option.annotation.JvmOptions;
import io.dekorate.option.config.JvmConfig;
import io.dekorate.option.configurator.ApplyJvmOptsConfigurator;

public interface JvmOptionsGenerator extends Generator  {

  String JVM = "jvm";
   
  @Override
  default String getKey() {
    return JVM;
  }
    
  @Override
  default void add(Element element) {
    JvmOptions jvmOptions = element.getAnnotation(JvmOptions.class);
    if (jvmOptions != null) {
      AnnotationConfiguration<JvmConfig> config = new AnnotationConfiguration<>(JvmConfigAdapter.newBuilder(jvmOptions));
      on(config);
    }
  }

  @Override
  default void add(Map map) {
        on(new PropertyConfiguration<>(
            JvmConfigAdapter
            .newBuilder(propertiesMap(map, JvmOptions.class))
        ));
  }

  default void on(ConfigurationSupplier<JvmConfig> config) {
    Session session = getSession();
    session.configurators().add(config);
    session.configurators().add(new ApplyJvmOptsConfigurator(config));
  }
}
