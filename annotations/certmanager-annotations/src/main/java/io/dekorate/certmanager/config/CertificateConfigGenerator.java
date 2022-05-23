/**
 * Copyright 2018 The original authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.certmanager.config;

import java.util.Map;

import io.dekorate.ConfigurationGenerator;
import io.dekorate.certmanager.adapter.CertificateConfigAdapter;
import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.config.PropertyConfiguration;
import io.dekorate.kubernetes.config.Configuration;

public interface CertificateConfigGenerator extends ConfigurationGenerator {

  String CERTIFICATE = "certificate";

  default String getKey() {
    return CERTIFICATE;
  }

  default Class<? extends Configuration> getConfigType() {
    return CertificateConfig.class;
  }

  @Override
  default void addAnnotationConfiguration(Map map) {
    add(new AnnotationConfiguration<>(CertificateConfigAdapter.newBuilder(propertiesMap(map, CertificateConfig.class))));
  }

  @Override
  default void addPropertyConfiguration(Map map) {
    add(new PropertyConfiguration<>(CertificateConfigAdapter.newBuilder(propertiesMap(map, CertificateConfig.class))));
  }

  default void add(ConfigurationSupplier<CertificateConfig> config) {
    getConfigurationRegistry().add(config);
  }
}
