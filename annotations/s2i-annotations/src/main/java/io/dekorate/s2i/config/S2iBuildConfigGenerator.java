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

package io.dekorate.s2i.config;

import java.util.Map;

import io.dekorate.ConfigurationGenerator;
import io.dekorate.WithProject;
import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.config.PropertyConfiguration;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.s2i.adapter.S2iBuildConfigAdapter;

public interface S2iBuildConfigGenerator extends ConfigurationGenerator, WithProject {

  String S2I = "s2i";

  default String getKey() {
    return S2I;
  }

  default Class<? extends Configuration> getConfigType() {
    return S2iBuildConfig.class;
  }

  @Override
  default void addAnnotationConfiguration(Map map) {
    on(new AnnotationConfiguration<S2iBuildConfig>(S2iBuildConfigAdapter.newBuilder(propertiesMap(map, S2iBuildConfig.class))));
  }

  @Override
  default void addPropertyConfiguration(Map map) {
    on(new PropertyConfiguration<S2iBuildConfig>(S2iBuildConfigAdapter.newBuilder(propertiesMap(map, S2iBuildConfig.class))));
  }

  default void on(ConfigurationSupplier<S2iBuildConfig> config) {
    getConfigurationRegistry().add(config);
  }
}
