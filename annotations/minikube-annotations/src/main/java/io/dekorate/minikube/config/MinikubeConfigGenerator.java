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
package io.dekorate.minikube.config;

import java.util.Map;

import io.dekorate.ConfigurationGenerator;
import io.dekorate.WithProject;
import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.config.PropertyConfiguration;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.minikube.adapter.MinikubeConfigAdapter;

public interface MinikubeConfigGenerator extends ConfigurationGenerator, WithProject {

  String MINIKUBE = "minikube";

  default String getKey() {
    return MINIKUBE;
  }

  default Class<? extends Configuration> getConfigType() {
    return MinikubeConfig.class;
  }

  @Override
  default void addAnnotationConfiguration(Map map) {
    add(new AnnotationConfiguration<>(MinikubeConfigAdapter.newBuilder(propertiesMap(map, MinikubeConfig.class))));
  }

  @Override
  default void addPropertyConfiguration(Map map) {
    add(new PropertyConfiguration<>(MinikubeConfigAdapter.newBuilder(propertiesMap(map, MinikubeConfig.class))));
  }

  default void add(ConfigurationSupplier<MinikubeConfig> config) {
    getConfigurationRegistry().add(config);
  }

}
