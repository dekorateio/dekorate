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
package io.dekorate.tekton.generator;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;

import javax.lang.model.element.Element;

import io.dekorate.BuildService;
import io.dekorate.BuildServiceFactories;
import io.dekorate.DekorateException;
import io.dekorate.Generator;
import io.dekorate.Session;
import io.dekorate.SessionListener;
import io.dekorate.WithProject;
import io.dekorate.WithSession;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.hook.ImageBuildHook;
import io.dekorate.tekton.adapter.TektonConfigAdapter;
import io.dekorate.tekton.annotation.TektonApplication;
import io.dekorate.tekton.config.TektonConfig;
import io.dekorate.tekton.config.TektonConfigCustomAdapter;
import io.dekorate.tekton.handler.TektonHandler;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.kubernetes.configurator.ApplyBuildToImageConfiguration;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.project.Project;

public interface TektonApplicationGenerator extends Generator, WithSession, WithProject, SessionListener {

  String TEKTON = "tekton";

  default String getKey() {
    return TEKTON;
  }

  default Class<? extends Annotation> getAnnotation() {
    return TektonApplication.class;
  }


  default void add(Element element) {
    TektonApplication tektonApplication = element.getAnnotation(TektonApplication.class);
    TektonConfig tektonConfig = TektonConfigCustomAdapter.newBuilder(getProject(), tektonApplication).build();

    on(new ConfigurationSupplier<>(TektonConfigAdapter.newBuilder(element.getAnnotation(TektonApplication.class))
                                   .accept(new ApplyBuildToImageConfiguration())
                                   .accept(new ApplyProjectInfo(getProject()))));
  }

  default void add(Map map) {
    TektonConfig tektonConfig = TektonConfigAdapter.newBuilder((Map) map.get(TektonApplication.class.getName())).build();
    on(new ConfigurationSupplier<>(TektonConfigAdapter.newBuilder(propertiesMap(map, TektonApplication.class))
                                   .accept(new ApplyBuildToImageConfiguration())
                                   .accept(new ApplyProjectInfo(getProject()))));
  }

    default void on(ConfigurationSupplier<TektonConfig> config) {
      Session session = getSession();
      session.configurators().add(config);
      session.handlers().add(new TektonHandler(session.resources(), session.configurators()));
      session.addListener(this);
  }

  default void onClosed() {
    //We need to set the TCCL, becuase the KubernetesClient used in this part of code, needs TCCL so that java.util.ServiceLoader can work.
    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
    try {
      Session session = getSession();
      Project project = getProject();
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      TektonConfig config = session.configurators().get(TektonConfig.class).get();
    } finally {
      Thread.currentThread().setContextClassLoader(tccl);
    }
  }
}
