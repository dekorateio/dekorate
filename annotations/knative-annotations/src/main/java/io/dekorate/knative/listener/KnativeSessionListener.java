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

package io.dekorate.knative.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.dekorate.BuildService;
import io.dekorate.BuildServiceFactories;
import io.dekorate.DekorateException;
import io.dekorate.Session;
import io.dekorate.SessionListener;
import io.dekorate.WithProject;
import io.dekorate.WithSession;
import io.dekorate.hook.ImageBuildHook;
import io.dekorate.hook.OrderedHook;
import io.dekorate.hook.ProjectHook;
import io.dekorate.hook.ResourcesApplyHook;
import io.dekorate.knative.config.KnativeConfig;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.project.Project;
import io.fabric8.kubernetes.api.model.KubernetesList;

public class KnativeSessionListener implements SessionListener, WithProject, WithSession {

  private static final String KNATIVE = "knative";

  @Override
  public void onClosed() {
    //We need to set the TTCL, becuase the KubenretesClient used in this part of code, needs TTCL so that java.util.ServiceLoader can work.
    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
    List<ProjectHook> hooks = new ArrayList<>();
    try {
      Session session = getSession();
      Project project = getProject();
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      KnativeConfig config = session.getConfigurationRegistry().get(KnativeConfig.class).get();
      Optional<ImageConfiguration> imageConfiguration = session.getConfigurationRegistry()
          .getImageConfig(BuildServiceFactories.supplierMatches(project));
      imageConfiguration.ifPresent(i -> {
        String name = i.getName();
        if (i.isAutoBuildEnabled() || config.isAutoDeployEnabled()) {
          KubernetesList generated = session.getGeneratedResources().get(KNATIVE);
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

      if (config.isAutoDeployEnabled()) {
        hooks.add(new ResourcesApplyHook(getProject(), KNATIVE, "kubectl"));
      }
    } finally {
      Thread.currentThread().setContextClassLoader(tccl);
      if (!hooks.isEmpty()) {
        OrderedHook hook = OrderedHook.create(hooks.toArray(new ProjectHook[hooks.size()]));
        hook.register();
      }
    }
  }
}
