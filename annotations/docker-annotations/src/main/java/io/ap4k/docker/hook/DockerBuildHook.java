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
package io.ap4k.docker.hook;

import io.ap4k.Coordinates;
import io.ap4k.docker.config.DockerBuildConfig;
import io.ap4k.hook.ProjectHook;
import io.ap4k.project.Project;
import io.ap4k.utils.Images;

import java.io.File;

public class DockerBuildHook extends ProjectHook {

  private final File dockerFile;
  private final String image;

  public DockerBuildHook(Project project, Coordinates coordinates, DockerBuildConfig config ) {
    super(project);
    this.dockerFile = project.getRoot().resolve(config.getDockerFile()).toFile();
    this.image = Images.getImage(config.getRegistry(), coordinates.getGroup(), coordinates.getName(), coordinates.getVersion());
  }

  @Override
  public void init() {

  }

  @Override
  public void warmup() {

  }

  @Override
  public void run() {
    exec("docker", "build", "-t" + image, project.getRoot().toAbsolutePath().toString());
  }
}
