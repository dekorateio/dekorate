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
package io.ap4k.option.generator;

import io.ap4k.Generator;
import io.ap4k.config.ConfigurationSupplier;
import io.ap4k.option.adapter.JvmConfigAdapter;
import io.ap4k.option.annotation.JvmOptions;
import io.ap4k.option.config.JvmConfig;
import io.ap4k.option.configurator.ApplyJvmOptsConfigurator;

import javax.lang.model.element.Element;
import java.util.Map;

public interface JvmOptionsGenerator extends Generator  {

  @Override
  default void add(Element element) {
    JvmOptions jvmOptions = element.getAnnotation(JvmOptions.class);
    if (jvmOptions != null) {
      ConfigurationSupplier<JvmConfig> config = new ConfigurationSupplier<>(JvmConfigAdapter.newBuilder(jvmOptions));
      on(config);
    }
  }

  @Override
  default void add(Map map) {
        on(new ConfigurationSupplier<>(
            JvmConfigAdapter
            .newBuilder(propertiesMap(map, JvmOptions.class))
        ));
  }

  default void on(ConfigurationSupplier<JvmConfig> config) {
    session.configurators().add(config);
    session.configurators().add(new ApplyJvmOptsConfigurator(config));
  }
}
