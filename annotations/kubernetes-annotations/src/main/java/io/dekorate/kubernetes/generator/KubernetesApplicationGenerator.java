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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.lang.model.element.Element;

import io.dekorate.BuildService;
import io.dekorate.BuildServiceFactories;
import io.dekorate.BuildServiceFactory;
import io.dekorate.DekorateException;
import io.dekorate.Generator;
import io.dekorate.Resources;
import io.dekorate.Session;
import io.dekorate.SessionListener;
import io.dekorate.WithProject;
import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.config.PropertyConfiguration;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.deps.kubernetes.client.DefaultKubernetesClient;
import io.dekorate.deps.kubernetes.client.KubernetesClient;
import io.dekorate.hook.ImageBuildHook;
import io.dekorate.hook.ImagePushHook;
import io.dekorate.hook.OrderedHook;
import io.dekorate.hook.ProjectHook;
import io.dekorate.kubernetes.adapter.KubernetesConfigAdapter;
import io.dekorate.kubernetes.annotation.KubernetesApplication;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.kubernetes.config.ImageConfigurationBuilder;
import io.dekorate.kubernetes.config.KubernetesConfig;
import io.dekorate.kubernetes.configurator.ApplyDeploy;
import io.dekorate.kubernetes.configurator.ApplyBuild;
import io.dekorate.kubernetes.handler.KubernetesHandler;
import io.dekorate.kubernetes.hook.ScaleDeploymentHook;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.project.Project;

public interface KubernetesApplicationGenerator extends Generator, SessionListener, WithProject {

  String KUBERNETES = "kubernetes";

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
            .accept(new ApplyBuild())
            .accept(new ApplyDeploy())
            .accept(new ApplyProjectInfo(getProject()))));
  }

  default void add(Element element) {
    KubernetesApplication application = element.getAnnotation(KubernetesApplication.class);
     add(new AnnotationConfiguration<>(
            KubernetesConfigAdapter
            .newBuilder(application)
            .accept(new ApplyBuild())
            .accept(new ApplyDeploy())
            .accept(new ApplyProjectInfo(getProject()))));
  }

  default void add(ConfigurationSupplier<KubernetesConfig> config)  {
    Session session = getSession();
    session.configurators().add(config);
    session.handlers().add(new KubernetesHandler(session.resources()));
    session.addListener(this);
  }

  default void onClosed() {
    Session session = getSession();
    Project project = getProject();
    Optional<KubernetesConfig> config = session.configurators().get(KubernetesConfig.class);
    Optional<ImageConfiguration> imageConfiguration = session.configurators().get(ImageConfiguration.class, BuildServiceFactories.matches(project));
    if (!config.isPresent()) {
      return;
    }

    KubernetesConfig kubernetesConfig = config.get();
    Resources resources = session.resources();
    KubernetesList generated = session.getGeneratedResources().getOrDefault(KUBERNETES, new KubernetesList());

    BuildService buildService = null;
    if (kubernetesConfig.isAutoPushEnabled() || kubernetesConfig.isAutoBuildEnabled()) {
      try {
          buildService = imageConfiguration.map(BuildServiceFactories.create(getProject(), generated.getItems())).orElseThrow(() -> new IllegalStateException("No applicable BuildServiceFactory found."));
      } catch (Exception e) {
        throw DekorateException.launderThrowable("Failed to lookup BuildService.", e);
      }
    }

    if (kubernetesConfig.isAutoPushEnabled()) {
      // When deploy is enabled, we scale the Deployment down before push
      // then scale it back up once the image has been successfully pushed
      // This ensure that the pod runs the proper image
      List<ProjectHook> hooks = new ArrayList<>();
      if (kubernetesConfig.isAutoDeployEnabled()) {
        try (KubernetesClient client = new DefaultKubernetesClient()) {
          client.resourceList(generated).createOrReplace();
        }
        hooks.add(new ScaleDeploymentHook(getProject(), session.resources().getName(), 0));
      }

      hooks.add(new ImageBuildHook(getProject(), buildService));
      hooks.add(new ImagePushHook(getProject(), buildService));
      if (kubernetesConfig.isAutoDeployEnabled()) {
        hooks.add(new ScaleDeploymentHook(getProject(), session.resources().getName(), 1));
      }
      OrderedHook hook = OrderedHook.create(hooks.toArray(new ProjectHook[hooks.size()]));
      hook.register();
    } else if (kubernetesConfig.isAutoBuildEnabled()) {
      new ImageBuildHook(getProject(), buildService).register();;
    }
  }
}
