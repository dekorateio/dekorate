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
import io.dekorate.ResourceRegistry;
import io.dekorate.Session;
import io.dekorate.SessionListener;
import io.dekorate.WithProject;
import io.dekorate.WithSession;
import io.dekorate.hook.ImageBuildHook;
import io.dekorate.hook.ImageLoadHook;
import io.dekorate.hook.ImagePushHook;
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
    Session session = getSession();
    Project project = getProject();
    Optional<KnativeConfig> optionalAppConfig = session.getConfigurationRegistry().get(KnativeConfig.class);
    Optional<ImageConfiguration> optionalImageConfig = session.getConfigurationRegistry()
        .getImageConfig(BuildServiceFactories.supplierMatches(project));
    if (!optionalAppConfig.isPresent() || !optionalImageConfig.isPresent()) {
      return;
    }

    KnativeConfig kubernetesConfig = optionalAppConfig.get();
    ResourceRegistry resources = session.getResourceRegistry();
    KubernetesList generated = session.getGeneratedResources().getOrDefault(KNATIVE, new KubernetesList());

    BuildService buildService = null;
    ImageConfiguration imageConfig = optionalImageConfig.get();
    if (imageConfig.isAutoBuildEnabled() || imageConfig.isAutoPushEnabled() || kubernetesConfig.isAutoDeployEnabled()
        || imageConfig.isAutoLoadEnabled()) {

      try {
        buildService = optionalImageConfig.map(BuildServiceFactories.create(getProject(), generated.getItems()))
            .orElseThrow(() -> new IllegalStateException("No applicable BuildServiceFactory found."));
      } catch (Exception e) {
        BuildServiceFactories.log(project, session.getConfigurationRegistry().getAll(ImageConfiguration.class));
        throw DekorateException.launderThrowable("Failed to lookup BuildService.", e);
      }
    }

    List<ProjectHook> hooks = new ArrayList<>();

    if (imageConfig.isAutoPushEnabled()) {
      hooks.add(new ImageBuildHook(getProject(), buildService));
      hooks.add(new ImagePushHook(getProject(), buildService));
    } else if (imageConfig.isAutoBuildEnabled() || kubernetesConfig.isAutoDeployEnabled()) {
      hooks.add(new ImageBuildHook(getProject(), buildService));
      hooks.add(new ImageLoadHook(getProject(), imageConfig));
    }

    if (kubernetesConfig.isAutoDeployEnabled()) {
      hooks.add(new ResourcesApplyHook(getProject(), KNATIVE, "kubectl"));
    }

    if (!hooks.isEmpty()) {
      OrderedHook hook = OrderedHook.create(hooks.toArray(new ProjectHook[hooks.size()]));
      hook.register();
    }
  }
}
