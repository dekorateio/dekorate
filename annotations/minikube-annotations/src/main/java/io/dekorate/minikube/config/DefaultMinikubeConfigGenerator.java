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

import io.dekorate.ConfigurationRegistry;
import io.dekorate.config.DefaultConfiguration;
import io.dekorate.kubernetes.configurator.PopulateNodePort;
import io.dekorate.minikube.configurator.ApplyServiceTypeNodePortConfigurator;
import io.dekorate.project.ApplyProjectInfo;

public class DefaultMinikubeConfigGenerator implements MinikubeConfigGenerator {

  private final ConfigurationRegistry configurationRegistry;

  public DefaultMinikubeConfigGenerator(ConfigurationRegistry configurationRegistry) {
    this.configurationRegistry = configurationRegistry;
    this.configurationRegistry.add(new ApplyProjectInfo(getProject()));
    this.configurationRegistry.add(new ApplyServiceTypeNodePortConfigurator());
    this.configurationRegistry.add(new PopulateNodePort());
    add(new DefaultConfiguration<MinikubeConfig>(MinikubeConfig.newMinikubeConfigBuilderFromDefaults()));
  }

  @Override
  public ConfigurationRegistry getConfigurationRegistry() {
    return this.configurationRegistry;
  }
}
