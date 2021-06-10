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

package io.dekorate.jaxrs.config;

import java.util.Map;

import io.dekorate.ConfigurationGenerator;
import io.dekorate.WithProject;
import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.config.PropertyConfiguration;
import io.dekorate.jaxrs.adapter.JaxrsConfigAdapter;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.configurator.SetPortPath;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.utils.Ports;
import io.dekorate.utils.Strings;

public interface JaxrsConfigGenerator extends ConfigurationGenerator, WithProject {

  String JAXRS = "jaxrs";

  default String getKey() {
    return JAXRS;
  }

  default Class<? extends Configuration> getConfigType() {
    return JaxrsConfig.class;
  }

  @Override
  default void addAnnotationConfiguration(Map map) {
    add(new AnnotationConfiguration<>(
        JaxrsConfigAdapter.newBuilder(propertiesMap(map, JaxrsConfig.class)).accept(new ApplyProjectInfo(getProject()))));
  }

  @Override
  default void addPropertyConfiguration(Map map) {
    add(new PropertyConfiguration<>(
        JaxrsConfigAdapter.newBuilder(propertiesMap(map, JaxrsConfig.class)).accept(new ApplyProjectInfo(getProject()))));
  }

  default void add(ConfigurationSupplier<JaxrsConfig> config) {
    getConfigurationRegistry().add(config);
    if (Strings.isNotNullOrEmpty(config.get().getPath())) {
      getConfigurationRegistry().add(new SetPortPath(Ports.PORT_PREDICATE, config.get().getPath()));
    }
  }
}
