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
package io.dekorate.jib.generator;

import java.util.Map;

import io.dekorate.Generator;
import io.dekorate.Session;
import io.dekorate.WithProject;
import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.config.PropertyConfiguration;
import io.dekorate.jib.adapter.JibBuildConfigAdapter;
import io.dekorate.jib.config.JibBuildConfig;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.configurator.ApplyBuildToImageConfiguration;
import io.dekorate.project.ApplyProjectInfo;

public interface JibGenerator extends Generator, WithProject {

  String JIB = "jib";

  default String getKey() {
    return JIB;
  }

  @Override
  default Class<? extends Configuration> getConfigType() {
    return JibBuildConfig.class;
  }

  @Override
  default void addAnnotationConfiguration(Map map) {
    on(new AnnotationConfiguration<>(
        JibBuildConfigAdapter
            .newBuilder(propertiesMap(map, JibBuildConfig.class))
            .accept(new ApplyProjectInfo(getProject()))
            .accept(new ApplyBuildToImageConfiguration())));
  }

  @Override
  default void addPropertyConfiguration(Map map) {
    on(new PropertyConfiguration<>(
        JibBuildConfigAdapter
            .newBuilder(propertiesMap(map, JibBuildConfig.class))
            .accept(new ApplyProjectInfo(getProject()))
            .accept(new ApplyBuildToImageConfiguration())));
  }

  default void on(ConfigurationSupplier<JibBuildConfig> config) {
    Session session = getSession();
    session.configurators().add(config);
  }

}
