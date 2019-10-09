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

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.lang.model.element.Element;

import io.dekorate.Generator;
import io.dekorate.Session;
import io.dekorate.WithProject;
import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.config.PropertyConfiguration;
import io.dekorate.kubernetes.adapter.KubernetesConfigAdapter;
import io.dekorate.kubernetes.annotation.KubernetesApplication;
import io.dekorate.kubernetes.config.KubernetesConfig;
import io.dekorate.kubernetes.configurator.ApplyDeployToImageConfiguration;
import io.dekorate.kubernetes.configurator.ApplyDeployToKubernetesConfiguration;
import io.dekorate.kubernetes.configurator.ApplyBuildToImageConfiguration;
import io.dekorate.kubernetes.configurator.ApplyBuildToKubernetesConfiguration;
import io.dekorate.kubernetes.handler.KubernetesHandler;
import io.dekorate.kubernetes.listener.KubernetesSessionListener;
import io.dekorate.project.ApplyProjectInfo;

public interface KubernetesApplicationGenerator extends Generator, WithProject {

  String KUBERNETES = "kubernetes";
  KubernetesSessionListener LISTENER = new KubernetesSessionListener();

  default String getKey() {
    return KUBERNETES;
  }

  default Class<? extends Annotation> getAnnotation() {
    return KubernetesApplication.class;
  }

  @Override
  default void add(Map map) {
        add(new PropertyConfiguration<>(
            KubernetesConfigAdapter
            .newBuilder(propertiesMap(map, KubernetesApplication.class))
            .accept(new ApplyBuildToImageConfiguration())
            .accept(new ApplyDeployToImageConfiguration())
            .accept(new ApplyBuildToKubernetesConfiguration())
            .accept(new ApplyDeployToKubernetesConfiguration())
            .accept(new ApplyProjectInfo(getProject()))));
  }

  default void add(Element element) {
    KubernetesApplication application = element.getAnnotation(KubernetesApplication.class);
     add(new AnnotationConfiguration<>(
            KubernetesConfigAdapter
            .newBuilder(application)
            .accept(new ApplyBuildToImageConfiguration())
            .accept(new ApplyDeployToImageConfiguration())
            .accept(new ApplyBuildToKubernetesConfiguration())
            .accept(new ApplyDeployToKubernetesConfiguration())
            .accept(new ApplyProjectInfo(getProject()))));
  }

  default void add(ConfigurationSupplier<KubernetesConfig> config)  {
    Session session = getSession();
    session.configurators().add(config);
    session.handlers().add(new KubernetesHandler(session.resources(), session.configurators()));
    session.addListener(LISTENER);
  }
}
