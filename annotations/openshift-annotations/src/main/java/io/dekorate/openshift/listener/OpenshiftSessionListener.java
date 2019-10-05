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
 * 
**/

package io.dekorate.openshift.listener;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.dekorate.BuildService;
import io.dekorate.BuildServiceFactories;
import io.dekorate.Session;
import io.dekorate.SessionListener;
import io.dekorate.WithProject;
import io.dekorate.WithSession;
import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.hook.ImageBuildHook;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.openshift.config.OpenshiftConfig;
import io.dekorate.openshift.config.OpenshiftConfigBuilder;
import io.dekorate.project.Project;

public class OpenshiftSessionListener implements SessionListener, WithProject, WithSession {

  private static final String DEFAULT_S2I_BUILDER_IMAGE = "fabric8/s2i-java:2.3";

 private static final OpenshiftConfig DEFAULT_SOURCE_TO_IMAGE_CONFIG = new OpenshiftConfigBuilder()
    .withBuilderImage(DEFAULT_S2I_BUILDER_IMAGE)
    .build();


  @Override
  public void onClosed() {
    Session session = getSession();
    Project project = getProject();
    // We ned to set the TTCL, becuase the KubenretesClient used in this part of
    // code, needs TTCL so that java.util.ServiceLoader can work.
    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      Optional<ImageConfiguration> imageConfiguration = session.configurators().get(ImageConfiguration.class,
          BuildServiceFactories.matches(project));
      OpenshiftConfig config = session.configurators().get(OpenshiftConfig.class)
          .orElse(DEFAULT_SOURCE_TO_IMAGE_CONFIG);
      String name = session.configurators().get(OpenshiftConfig.class).map(c -> c.getName())
          .orElse(getProject().getBuildInfo().getName());
      if (config.isAutoBuildEnabled() || config.isAutoDeployEnabled()) {

        KubernetesList list = session.getGeneratedResources().get("openshift");
        List<HasMetadata> generated = list != null ? list.getItems() : Collections.emptyList();

        BuildService buildService = imageConfiguration.map(BuildServiceFactories.create(project, generated))
            .orElseThrow(() -> new IllegalStateException("No applicable BuildServiceFactory found."));
        new ImageBuildHook(getProject(), buildService).register();
      }
    } finally {
      Thread.currentThread().setContextClassLoader(tccl);
    }

  }
}
