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
package io.dekorate.kubernetes.generator;

import java.util.Map;

import javax.lang.model.element.Element;

import io.dekorate.Generator;
import io.dekorate.Session;
import io.dekorate.WithProject;
import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.config.PropertyConfiguration;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.dekorate.kubernetes.adapter.KubernetesConfigAdapter;
import io.dekorate.kubernetes.annotation.KubernetesApplication;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.KubernetesConfig;
import io.dekorate.kubernetes.configurator.ApplyBuildToImageConfiguration;
import io.dekorate.kubernetes.configurator.ApplyDeployToApplicationConfiguration;
import io.dekorate.kubernetes.configurator.ApplyImagePullSecretConfiguration;
import io.dekorate.kubernetes.handler.KubernetesHandler;
import io.dekorate.kubernetes.listener.KubernetesSessionListener;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.utils.Strings;

public interface KubernetesApplicationGenerator extends Generator, WithProject {

  String KUBERNETES = "kubernetes";
  KubernetesSessionListener LISTENER = new KubernetesSessionListener();

  default String getKey() {
    return KUBERNETES;
  }

  default Class<? extends Configuration> getConfigType() {
    return KubernetesConfig.class;
  }

  @Override
  default void addAnnotationConfiguration(Map map) {
        add(new AnnotationConfiguration<>(
            KubernetesConfigAdapter
            .newBuilder(propertiesMap(map, KubernetesConfig.class))
            .accept(new ApplyBuildToImageConfiguration())
            .accept(new ApplyImagePullSecretConfiguration())
            .accept(new ApplyDeployToApplicationConfiguration())
            .accept(new ApplyProjectInfo(getProject()))));
  }


  @Override
  default void addPropertyConfiguration(Map map) {
        add(new PropertyConfiguration<>(
            KubernetesConfigAdapter
            .newBuilder(propertiesMap(map, KubernetesConfig.class))
            .accept(new ApplyBuildToImageConfiguration())
            .accept(new ApplyImagePullSecretConfiguration())
            .accept(new ApplyDeployToApplicationConfiguration())
            .accept(new ApplyProjectInfo(getProject()))));
  }

  default void add(ConfigurationSupplier<KubernetesConfig> config)  {
    Session session = getSession();
    session.configurators().add(config);
    session.resources().groups().putIfAbsent(KUBERNETES, new KubernetesListBuilder());
    session.handlers().add(new KubernetesHandler(session.resources(), session.configurators()));
    session.addListener(LISTENER);
  }
}
