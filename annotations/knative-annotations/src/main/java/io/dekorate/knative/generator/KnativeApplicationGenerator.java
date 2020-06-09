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
import java.util.Optional;

import io.dekorate.BuildService;
import io.dekorate.BuildServiceFactories;
import io.dekorate.DekorateException;
import io.dekorate.Generator;
import io.dekorate.Session;
import io.dekorate.SessionListener;
import io.dekorate.WithProject;
import io.dekorate.WithSession;
import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.config.PropertyConfiguration;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.hook.ImageBuildHook;
import io.dekorate.knative.adapter.KnativeConfigAdapter;
import io.dekorate.knative.config.KnativeConfig;
import io.dekorate.knative.handler.KnativeHandler;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.kubernetes.configurator.ApplyBuildToImageConfiguration;
import io.dekorate.kubernetes.configurator.ApplyImagePullSecretConfiguration;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.project.Project;

public interface KnativeApplicationGenerator extends Generator, WithSession, WithProject, SessionListener {

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
      Session session = getSession();
      session.configurators().add(config);
      session.handlers().add(new KnativeHandler(session.resources(), session.configurators()));
      session.addListener(this);
  }

  default void onClosed() {
    //We ned to set the TTCL, becuase the KubenretesClient used in this part of code, needs TTCL so that java.util.ServiceLoader can work.
    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
    try {
      Session session = getSession();
      Project project = getProject();
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      KnativeConfig config = session.configurators().get(KnativeConfig.class).get();
      Optional<ImageConfiguration> imageConfiguration = session.configurators().getImageConfig(BuildServiceFactories.supplierMatches(project));
      imageConfiguration.ifPresent(i -> {
        String name = i.getName();
        if (i.isAutoBuildEnabled() || config.isAutoDeployEnabled()) {
          KubernetesList generated = session.getGeneratedResources().get("knative");
          BuildService buildService;
          try {
            buildService = imageConfiguration.map(BuildServiceFactories.create(getProject(), generated.getItems()))
                .orElseThrow(() -> new IllegalStateException("No applicable BuildServiceFactory found."));
          } catch (Exception e) {
            throw DekorateException.launderThrowable("Failed to lookup BuildService.", e);
          }

          ImageBuildHook hook = new ImageBuildHook(getProject(), buildService);
          hook.register();
        }
      });
    } finally {
      Thread.currentThread().setContextClassLoader(tccl);
    }
  }
}
