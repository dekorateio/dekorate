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
package io.dekorate.knative.config;

import java.util.Map;

import io.dekorate.ConfigurationGenerator;
import io.dekorate.WithProject;
import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.config.PropertyConfiguration;
import io.dekorate.knative.adapter.KnativeConfigAdapter;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.configurator.ApplyBuildToImageConfiguration;
import io.dekorate.kubernetes.configurator.ApplyImagePullSecretConfiguration;
import io.dekorate.project.ApplyProjectInfo;

public interface KnativeConfigGenerator extends ConfigurationGenerator, WithProject {

  String KNATIVE = "knative";

  default String getKey() {
    return KNATIVE;
  }

  default Class<? extends Configuration> getConfigType() {
    return KnativeConfig.class;
  }

  default void addAnnotationConfiguration(Map map) {
    on(new AnnotationConfiguration<>(KnativeConfigAdapter.newBuilder(propertiesMap(map, KnativeConfig.class))
        .accept(new ApplyImagePullSecretConfiguration())
        .accept(new ApplyBuildToImageConfiguration())
        .accept(new ApplyProjectInfo(getProject()))));
  }

  default void addPropertyConfiguration(Map map) {
    on(new PropertyConfiguration<>(KnativeConfigAdapter.newBuilder(propertiesMap(map, KnativeConfig.class))
        .accept(new ApplyImagePullSecretConfiguration())
        .accept(new ApplyBuildToImageConfiguration())
        .accept(new ApplyProjectInfo(getProject()))));
  }

  default void on(ConfigurationSupplier<KnativeConfig> config) {
    getConfigurationRegistry().add(config);
  }
}
