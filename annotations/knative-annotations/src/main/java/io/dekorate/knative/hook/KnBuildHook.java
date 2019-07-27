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
package io.dekorate.knative.hook;

import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.deps.knative.serving.v1beta1.*;
import io.dekorate.deps.kubernetes.client.DefaultKubernetesClient;
import io.dekorate.deps.kubernetes.client.KubernetesClient;
import io.dekorate.deps.knative.client.DefaultKnativeClient;
import io.dekorate.deps.knative.client.KnativeClient;
import io.dekorate.hook.ProjectHook;
import io.dekorate.knative.config.KnativeConfig;
import io.dekorate.knative.util.KnativeUtils;
import io.dekorate.project.Project;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class KnBuildHook extends ProjectHook {

  private final String name;
  private final KnativeConfig config;
  private final KubernetesClient client = new DefaultKubernetesClient();
  private final KnativeClient kn = new DefaultKnativeClient();
  private final KubernetesList kubernetesList;

  public KnBuildHook(String name, KnativeConfig config, Project project, KubernetesList kubernetesList) {
    super(project);
    this.name = name;
    this.config = config;
    this.kubernetesList = kubernetesList;
  }

  public void init () {
    final List<HasMetadata> items = kubernetesList.getItems();
    items.stream()
            .filter(i -> config.isAutoDeployEnabled())
            .forEach(i -> {
              HasMetadata item = client.resource(i).createOrReplace();
              System.out.println("Applied: " + item.getKind() + " " + i.getMetadata().getName());
            });
  }

  @Override
  public void warmup() {

  }

  @Override
  public void run() {
    if (project.getBuildInfo().getOutputFile().getParent().toFile().exists()) {
      exec("oc", "start-build", name, "--from-dir=" + project.getBuildInfo().getOutputFile().getParent().toAbsolutePath().toString(), "--follow");
    } else {
     throw new IllegalStateException("Can't trigger binary build. " + project.getBuildInfo().getOutputFile().toAbsolutePath().toString() + " does not exist!");
    }
  }
}
