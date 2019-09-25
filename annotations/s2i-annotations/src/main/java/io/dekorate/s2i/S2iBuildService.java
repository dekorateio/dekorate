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
package io.dekorate.s2i;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import io.dekorate.BuildService;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.project.Project;
import io.dekorate.utils.Exec;
import io.dekorate.s2i.util.S2iUtils;

import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.Secret;
import io.dekorate.deps.openshift.api.model.Build;
import io.dekorate.deps.openshift.api.model.BuildConfig;
import io.dekorate.deps.openshift.api.model.ImageStream;
import io.dekorate.deps.openshift.client.DefaultOpenShiftClient;
import io.dekorate.deps.openshift.client.OpenShiftClient;

public class S2iBuildService implements BuildService {

  private final Logger LOGGER = LoggerFactory.getLogger();
  
  private final Exec.ProjectExec exec;
  private final Project project;
  private final ImageConfiguration config;
  private final Collection<HasMetadata> resources;

  public S2iBuildService(Project project, ImageConfiguration config, Collection<HasMetadata> resources) {
    this.project = project;
    this.config = config;
    this.resources = resources;
    this.exec = Exec.inProject(project);
  }

  public void prepare() {
    try (OpenShiftClient client = new DefaultOpenShiftClient()) {
      resources.stream().filter(i -> i instanceof BuildConfig
          || i instanceof ImageStream || i instanceof Secret).forEach(i -> {
            HasMetadata item = client.resource(i).deletingExisting().createOrReplace();
            if (item instanceof BuildConfig) {
              client.builds().withLabel("openshift.io/build-config.name", item.getMetadata().getName()).delete();
            }
            LOGGER.info("Applied: " + item.getKind() + " " + i.getMetadata().getName());
          });
      S2iUtils.waitForImageStreamTags(resources, 2, TimeUnit.MINUTES);
    }
  }

  public void build() {
    prepare();
    if (project.getBuildInfo().getOutputFile().getParent().toFile().exists()) {
      LOGGER.info("Performing s2i build.");
      exec.commands("oc", "start-build", config.getName(), "--from-dir=" + project.getBuildInfo().getOutputFile().getParent().toAbsolutePath().toString(), "--follow");
    } else {
     throw new IllegalStateException("Can't trigger binary build. " + project.getBuildInfo().getOutputFile().toAbsolutePath().toString() + " does not exist!");
    }
  }

  public void push() {
  }
}
