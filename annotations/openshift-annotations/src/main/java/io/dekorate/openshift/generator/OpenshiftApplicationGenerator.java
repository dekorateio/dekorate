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
package io.dekorate.openshift.generator;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.lang.model.element.Element;

import io.dekorate.Generator;
import io.dekorate.Session;
import io.dekorate.WithProject;
import io.dekorate.WithSession;
import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.config.PropertyConfiguration;
import io.dekorate.deps.kubernetes.api.model.KubernetesListBuilder;
import io.dekorate.kubernetes.configurator.ApplyDeployToApplicationConfiguration;
import io.dekorate.openshift.adapter.OpenshiftConfigAdapter;
import io.dekorate.openshift.annotation.OpenshiftApplication;
import io.dekorate.openshift.config.OpenshiftConfig;
import io.dekorate.openshift.config.OpenshiftConfigCustomAdapter;
import io.dekorate.openshift.handler.OpenshiftHandler;
import io.dekorate.openshift.listener.OpenshiftSessionListener;
import io.dekorate.project.ApplyProjectInfo;

public interface OpenshiftApplicationGenerator extends Generator, WithSession, WithProject  {

  String OPENSHIFT = "openshift";
  OpenshiftSessionListener LISTENER = new OpenshiftSessionListener();

  default String getKey() {
    return OPENSHIFT;
  }

  default Class<? extends Annotation> getAnnotation() {
    return OpenshiftApplication.class;
  }


  default void add(Element element) {
    on(new AnnotationConfiguration<>(OpenshiftConfigAdapter.newBuilder(element.getAnnotation(OpenshiftApplication.class))
                                     .accept(new ApplyDeployToApplicationConfiguration())
                                     .accept(new ApplyProjectInfo(getProject()))));
  }

  default void add(Map map) {
    on(new PropertyConfiguration<>(OpenshiftConfigAdapter.newBuilder(propertiesMap(map, OpenshiftApplication.class))
                                   .accept(new ApplyDeployToApplicationConfiguration())
                                   .accept(new ApplyProjectInfo(getProject()))));
  }

    default void on(ConfigurationSupplier<OpenshiftConfig> config) {
      Session session = getSession();
      session.configurators().add(config);
      session.addListener(LISTENER);
      session.resources().groups().putIfAbsent(OPENSHIFT, new KubernetesListBuilder());
      session.handlers().add(new OpenshiftHandler(session.resources(), session.configurators()));
  }

}
