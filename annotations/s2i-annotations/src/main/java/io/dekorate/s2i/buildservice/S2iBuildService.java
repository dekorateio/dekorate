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
package io.dekorate.s2i.buildservice;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import io.dekorate.BuildService;
import io.dekorate.DekorateException;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.Secret;
import io.dekorate.deps.openshift.api.model.BuildConfig;
import io.dekorate.deps.openshift.api.model.BuildList;
import io.dekorate.deps.openshift.api.model.ImageStream;
import io.dekorate.deps.openshift.client.DefaultOpenShiftClient;
import io.dekorate.deps.openshift.client.OpenShiftClient;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.project.Project;
import io.dekorate.utils.Exec;
import io.dekorate.s2i.util.S2iUtils;

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
    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
    try (OpenShiftClient client = new DefaultOpenShiftClient()) {
      Thread.currentThread().setContextClassLoader(S2iBuildService.class.getClassLoader());
      BuildList builds = client.builds().withLabel("openshift.io/build-config.name", buildConfigName(resources)).list();
      builds.getItems().stream().forEach(b -> { LOGGER.info("Deleting stale build:"+b.getMetadata().getName()); client.resource(b).cascading(true).delete(); });
      resources.stream().filter(i -> i instanceof BuildConfig || i instanceof ImageStream || i instanceof Secret).forEach(i -> {
              if (i instanceof BuildConfig) {
                client.resource(i).cascading(true).delete();
                try {
                  client.resource(i).waitUntilCondition(d -> d == null, 10, TimeUnit.SECONDS);
                } catch (IllegalArgumentException e) {
                  LOGGER.warning(e.getMessage());
                  //We can should ignore that, as its expected to be thrown when item is actually deleted.
                } catch (InterruptedException e) {
                  LOGGER.warning(e.getMessage());
                  throw DekorateException.launderThrowable(e);
                }
             }
             client.resource(i).createOrReplace();
             LOGGER.info("Applied: " + i.getKind() + " " + i.getMetadata().getName());
          });
      S2iUtils.waitForImageStreamTags(resources, 2, TimeUnit.MINUTES);
    } finally {
      Thread.currentThread().setContextClassLoader(tccl);
    }
  }

  public void build() {
    if (project.getBuildInfo().getOutputFile().getParent().toFile().exists()) {
      LOGGER.info("Performing s2i build.");
      exec.commands("oc", "start-build", buildConfigName(resources), "--from-dir=" + project.getBuildInfo().getOutputFile().getParent().toAbsolutePath().toString(), "--follow");
    } else {
     throw new IllegalStateException("Can't trigger binary build. " + project.getBuildInfo().getOutputFile().toAbsolutePath().toString() + " does not exist!");
    }
  }

  public void push() {
  }

  private static String buildConfigName(Collection<HasMetadata> resources) {
    return resources.stream().filter(h -> "BuildConfig".equals(h.getKind())).map(h -> h.getMetadata().getName()).findFirst().orElseThrow(() -> new IllegalStateException("No build config found."));
  }
}
