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
package io.ap4k.openshift.hook;

import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.api.model.Secret;
import io.ap4k.deps.openshift.api.model.BuildConfig;
import io.ap4k.deps.openshift.api.model.ImageStream;
import io.ap4k.deps.openshift.client.DefaultOpenShiftClient;
import io.ap4k.deps.openshift.client.OpenShiftClient;
import io.ap4k.hook.ProjectHook;
import io.ap4k.openshift.config.OpenshiftConfig;
import io.ap4k.openshift.util.OpenshiftUtils;
import io.ap4k.project.Project;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class OcBuildHook extends ProjectHook {

  private final String name;
  private final OpenshiftConfig config;
  private final OpenShiftClient client = new DefaultOpenShiftClient();
  private final KubernetesList kubernetesList;

  public OcBuildHook(String name, OpenshiftConfig config, Project project, KubernetesList kubernetesList) {
    super(project);
    this.name = name;
    this.config = config;
    this.kubernetesList = kubernetesList;
  }

  public void init () {
    final List<HasMetadata> items = kubernetesList.getItems();
    items.stream()
            .filter(i -> config.isAutoDeployEnabled() || i instanceof BuildConfig || i instanceof ImageStream || i instanceof Secret)
            .forEach(i -> {
              HasMetadata item = client.resource(i).createOrReplace();
              System.out.println("Applied: " + item.getKind() + " " + i.getMetadata().getName());
            });
    OpenshiftUtils.waitForImageStreamTags(items, 2, TimeUnit.MINUTES);
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
