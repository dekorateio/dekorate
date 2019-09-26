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
package io.dekorate.knative.generator;

import java.util.Map;

import javax.lang.model.element.Element;

import io.dekorate.BuildService;
import io.dekorate.BuildServiceFactories;
import io.dekorate.BuildServiceFactory;
import io.dekorate.DekorateException;
import io.dekorate.Generator;
import io.dekorate.Session;
import io.dekorate.SessionListener;
import io.dekorate.WithProject;
import io.dekorate.WithSession;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.hook.ImageBuildHook;
import io.dekorate.knative.adapter.KnativeConfigAdapter;
import io.dekorate.knative.annotation.KnativeApplication;
import io.dekorate.knative.config.KnativeConfig;
import io.dekorate.knative.config.KnativeConfigCustomAdapter;
import io.dekorate.knative.handler.KnativeHandler;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.kubernetes.config.ImageConfigurationBuilder;
import io.dekorate.kubernetes.configurator.ApplyDeploy;
import io.dekorate.kubernetes.configurator.ApplyBuild;
import io.dekorate.project.ApplyProjectInfo;

public interface KnativeApplicationGenerator extends Generator, WithSession, WithProject, SessionListener {

  String KNATIVE = "knative";

  default void add(Element element) {
    KnativeApplication knativeApplication = element.getAnnotation(KnativeApplication.class);
    KnativeConfig knativeConfig = KnativeConfigCustomAdapter.newBuilder(getProject(), knativeApplication).build();

    on(new ConfigurationSupplier<>(KnativeConfigAdapter.newBuilder(element.getAnnotation(KnativeApplication.class))
                                   .accept(new ApplyBuild())
                                   .accept(new ApplyDeploy())
                                   .accept(new ApplyProjectInfo(getProject()))));
  }

  default void add(Map map) {
    KnativeConfig knativeConfig = KnativeConfigAdapter.newBuilder((Map) map.get(KnativeApplication.class.getName())).build();
    on(new ConfigurationSupplier<>(KnativeConfigAdapter.newBuilder(propertiesMap(map, KnativeApplication.class))
                                   .accept(new ApplyBuild())
                                   .accept(new ApplyDeploy())
                                   .accept(new ApplyProjectInfo(getProject()))));
  }

    default void on(ConfigurationSupplier<KnativeConfig> config) {
      Session session = getSession();
      session.configurators().add(config);
      session.handlers().add(new KnativeHandler(session.resources()));
      session.addListener(this);
  }

  default void onClosed() {
    //We ned to set the TTCL, becuase the KubenretesClient used in this part of code, needs TTCL so that java.util.ServiceLoader can work.
    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
    try {
      Session session = getSession();
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      KnativeConfig config = session.configurators().get(KnativeConfig.class).get();
      if (config != null) {
        String name = session.configurators().get(KnativeConfig.class).map(c -> c.getName()).orElse(getProject().getBuildInfo().getName());
        if (config.isAutoBuildEnabled() || config.isAutoDeployEnabled()) {
          ImageConfiguration imageConfiguration = new ImageConfigurationBuilder()
            .withName(config.getName())
            .withGroup(config.getGroup())
            .withVersion(config.getVersion())
            .withRegistry(config.getRegistry())
            .build();

          KubernetesList generated = session.getGeneratedResources().get("knative");

          BuildService buildService;
          try {
            BuildServiceFactory buildServiceFactory = BuildServiceFactories.find(getProject(), imageConfiguration)
              .orElseThrow(() -> new IllegalStateException("No applicable BuildServiceFactory found."));
            buildService = buildServiceFactory.create(getProject(), imageConfiguration, generated.getItems());
          } catch (Exception e) {
            throw DekorateException.launderThrowable("Failed to lookup BuildService.", e);
          }

          ImageBuildHook hook = new ImageBuildHook(getProject(), buildService);
          hook.register();
        }
      }
    } finally {
      Thread.currentThread().setContextClassLoader(tccl);
    }
  }
}
