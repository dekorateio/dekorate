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

import java.util.ArrayList;
import java.util.Collections;
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
import io.dekorate.hook.ImagePushHook;
import io.dekorate.hook.OrderedHook;
import io.dekorate.hook.ProjectHook;
import io.dekorate.hook.ResourcesApplyHook;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.openshift.config.OpenshiftConfig;
import io.dekorate.project.Project;
import io.dekorate.s2i.config.S2iBuildConfig;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;

public class OpenshiftSessionListener implements SessionListener, WithProject, WithSession {

  private final String OPENSHIFT = "openshift";

  @Override
  public void onClosed() {
    Session session = getSession();
    Project project = getProject();
    // We ned to set the TTCL, becuase the KubenretesClient used in this part of
    // code, needs TTCL so that java.util.ServiceLoader can work.
    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
    List<ProjectHook> hooks = new ArrayList<>();

    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      Optional<OpenshiftConfig> optionalAppConfig = session.getConfigurationRegistry().get(OpenshiftConfig.class);
      Optional<ImageConfiguration> optionalImageConfig = session.getConfigurationRegistry()
          .getImageConfig(BuildServiceFactories.supplierMatches(project));

      if (!optionalAppConfig.isPresent() || !optionalImageConfig.isPresent()) {
        return;
      }

      OpenshiftConfig openshiftConfig = optionalAppConfig.get();
      ImageConfiguration imageConfig = optionalImageConfig.get();

      String name = session.getConfigurationRegistry().get(OpenshiftConfig.class).map(c -> c.getName())
          .orElse(getProject().getBuildInfo().getName());

      BuildService buildService = null;
      boolean s2iEnabled = imageConfig instanceof S2iBuildConfig && ((S2iBuildConfig) imageConfig).isEnabled();
      if (imageConfig.isAutoBuildEnabled() || imageConfig.isAutoPushEnabled() || openshiftConfig.isAutoDeployEnabled()) {

        KubernetesList list = session.getGeneratedResources().get("openshift");
        List<HasMetadata> generated = list != null ? list.getItems() : Collections.emptyList();

        try {
          buildService = optionalImageConfig.map(BuildServiceFactories.create(getProject(), generated))
              .orElseThrow(() -> new IllegalStateException("No applicable BuildServiceFactory found."));
        } catch (Exception e) {
          BuildServiceFactories.log(project, session.getConfigurationRegistry().getAll(ImageConfiguration.class));
          throw DekorateException.launderThrowable("Failed to lookup BuildService.", e);
        }

        hooks.add(new ImageBuildHook(getProject(), buildService));
      }

      if (imageConfig.isAutoPushEnabled() && !s2iEnabled) {
        hooks.add(new ImagePushHook(getProject(), buildService));
      }

      if (openshiftConfig.isAutoDeployEnabled()) {
        hooks.add(new ResourcesApplyHook(getProject(), OPENSHIFT, "oc"));
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
