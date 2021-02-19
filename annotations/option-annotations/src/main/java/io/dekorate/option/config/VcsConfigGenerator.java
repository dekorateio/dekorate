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
package io.dekorate.option.config;

import java.util.Map;

import io.dekorate.ConfigurationGenerator;
import io.dekorate.Session;
import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.config.PropertyConfiguration;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.option.adapter.VcsConfigAdapter;

public interface VcsConfigGenerator extends ConfigurationGenerator {

  String VCS = "vcs";

  @Override
  default String getKey() {
    return VCS;
  }

  @Override
  default Class<? extends Configuration> getConfigType() {
    return VcsConfig.class;
  }

  @Override
  default void addAnnotationConfiguration(Map map) {
    on(new AnnotationConfiguration<>(VcsConfigAdapter.newBuilder(propertiesMap(map, VcsConfig.class))));
  }

  @Override
  default void addPropertyConfiguration(Map map) {
    on(new PropertyConfiguration<>(VcsConfigAdapter.newBuilder(propertiesMap(map, VcsConfig.class))));
  }

  default void on(ConfigurationSupplier<VcsConfig> config) {
    Session session = getSession();
    session.getConfigurationRegistry().add(config);
  }
}
