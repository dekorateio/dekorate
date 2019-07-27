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

import io.dekorate.WithProject;
import io.dekorate.WithSession;
import io.dekorate.Generator;
import io.dekorate.SessionListener;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.knative.adapter.KnativeConfigAdapter;
import io.dekorate.knative.annotation.KnativeApplication;
import io.dekorate.knative.config.KnativeConfig;
import io.dekorate.knative.config.KnativeConfigBuilder;
import io.dekorate.knative.config.KnativeConfigCustomAdapter;

import javax.lang.model.element.Element;

import io.dekorate.knative.handler.KnativeHandler;
import io.dekorate.knative.hook.KnBuildHook;
import io.dekorate.project.ApplyProjectInfo;

import java.util.Map;

public interface KnativeApplicationGenerator extends Generator, WithSession, WithProject, SessionListener {

  String KNATIVE = "knative";

  default void add(Element element) {
    KnativeApplication knativeApplication = element.getAnnotation(KnativeApplication.class);
    KnativeConfig knativeConfig = KnativeConfigCustomAdapter.newBuilder(getProject(), knativeApplication).build();

    on(new ConfigurationSupplier<>(KnativeConfigAdapter.newBuilder(element.getAnnotation(KnativeApplication.class))
                                   .accept(new ApplyProjectInfo(getProject()))));
  }

  default void add(Map map) {
    KnativeConfig knativeConfig = KnativeConfigAdapter.newBuilder((Map) map.get(KnativeApplication.class.getName())).build();
    on(new ConfigurationSupplier<>(KnativeConfigAdapter.newBuilder(propertiesMap(map, KnativeApplication.class))
                                   .accept(new ApplyProjectInfo(getProject()))));
  }

    default void on(ConfigurationSupplier<KnativeConfig> config) {
      session.configurators().add(config);
      session.handlers().add(new KnativeHandler(session.resources()));
      session.addListener(this);
  }

  default void onClosed() {
    //We ned to set the TTCL, becuase the KubenretesClient used in this part of code, needs TTCL so that java.util.ServiceLoader can work.
    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(KnBuildHook.class.getClassLoader());
      KnativeConfig config = session.configurators().get(KnativeConfig.class).get();
      if (config != null) {
        String name = session.configurators().get(KnativeConfig.class).map(c -> c.getName()).orElse(getProject().getBuildInfo().getName());
        if (config.isAutoBuildEnabled() || config.isAutoDeployEnabled()) {
          KnBuildHook hook = new KnBuildHook(name, config, getProject(), session.getGeneratedResources().get("knative"));
          hook.register();
        }
      }
    } finally {
      Thread.currentThread().setContextClassLoader(tccl);
    }
  }
}
