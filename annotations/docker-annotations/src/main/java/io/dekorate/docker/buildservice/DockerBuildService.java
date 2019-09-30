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

package io.dekorate.docker.buildservice;

import java.io.File;

import io.dekorate.BuildService;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.project.Project;
import io.dekorate.utils.Exec;
import io.dekorate.utils.Images;
import io.dekorate.utils.Strings;

public class DockerBuildService implements BuildService {
    
  private Logger LOGGER = LoggerFactory.getLogger();

  private final File dockerFile;
  private final String image;
  private final Exec.ProjectExec exec;

  private final Project project;
  private final ImageConfiguration config;

	public DockerBuildService(Project project, ImageConfiguration config) {
    this.project = project;
    this.config = config;

    this.exec = Exec.inProject(project);
    this.dockerFile = project.getRoot().resolve(Strings.isNotNullOrEmpty(config.getDockerFile()) ? config.getDockerFile() : "Dockerfile").toFile();
    this.image = Images.getImage(config.getRegistry(), config.getGroup(), config.getName(), config.getVersion());
	}


	@Override
	public void build() {
    LOGGER.info("Performing docker build.");
    exec.commands("docker", "build", "-f" + dockerFile.getAbsolutePath(), "-t" + image, project.getRoot().toAbsolutePath().toString());
	}

	@Override
	public void push() {
    LOGGER.info("Performing docker push.");
    exec.commands("docker", "push",  image);
	}
}
