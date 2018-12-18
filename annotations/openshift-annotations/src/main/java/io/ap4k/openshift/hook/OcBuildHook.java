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

import io.ap4k.Ap4kException;
import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.api.model.Secret;
import io.ap4k.deps.openshift.api.model.BuildConfig;
import io.ap4k.deps.openshift.api.model.ImageStream;
import io.ap4k.deps.openshift.client.DefaultOpenShiftClient;
import io.ap4k.deps.openshift.client.OpenShiftClient;
import io.ap4k.hook.ProjectHook;
import io.ap4k.openshift.config.S2iConfig;
import io.ap4k.openshift.util.OpenshiftUtils;
import io.ap4k.project.Project;
import io.ap4k.utils.Serialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OcBuildHook extends ProjectHook {

  private static final String OPENSHIFT_YML = "openshift.yml";

  private final S2iConfig config;
  private final OpenShiftClient client = new DefaultOpenShiftClient();

  public OcBuildHook(S2iConfig config, Project project) {
    super(project);
    this.config = config;
  }

  public void init () {
    File yml = Paths.get(project.getResourceOutputPath()).resolve(OPENSHIFT_YML).toFile();
    List<HasMetadata> items = new ArrayList<>();
    if (yml.exists()) try (FileInputStream fis = new FileInputStream(yml)) {
      items.addAll(Serialization.unmarshal(fis, KubernetesList.class).getItems());
      items.stream()
        .filter(i -> config.isAutoDeployEnabled() || i instanceof BuildConfig || i instanceof ImageStream || i instanceof Secret)
        .forEach(i -> {
          HasMetadata item = client.resource(i).createOrReplace();
          System.out.println("Applied: " + item.getKind() + " " + i.getMetadata().getName());
        });
      OpenshiftUtils.waitForImageStreamTags(items, 2, TimeUnit.MINUTES);
    } catch (IOException e) {
      Ap4kException.launderThrowable(e);
    }
  }

  @Override
  public void warmup() {

  }

  @Override
  public void run() {
    if (project.getBuildInfo().getOutputFile().getParent().toFile().exists()) {
      exec("oc", "start-build", config.getName(), "--from-dir=" + project.getBuildInfo().getOutputFile().getParent().toAbsolutePath().toString(), "--follow");
    } else {
     throw new IllegalStateException("Can't trigger binary build. " + project.getBuildInfo().getOutputFile().toAbsolutePath().toString() + " does not exist!");
    }
  }
}
