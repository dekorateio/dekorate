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

package io.dekorate.jib.buildservice;

import io.dekorate.BuildService;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.project.Project;
import io.dekorate.utils.Exec;
import io.dekorate.utils.Images;
import io.dekorate.utils.Strings;

public class JibBuildService implements BuildService {

  private final Logger LOGGER = LoggerFactory.getLogger();

  private final Project project;
  private final ImageConfiguration config;
  private final String image;
  private final Exec.ProjectExec exec;

	public JibBuildService(Project project, ImageConfiguration config) {
		this.project = project;
		this.config = config;
    this.exec = Exec.inProject(project);
    this.image = Images.getImage(Strings.isNullOrEmpty(config.getRegistry()) ? "docker.io" : config.getRegistry() , config.getGroup(), config.getName(), config.getVersion());
	}


	@Override
	public void build() {
    LOGGER.info("Performing jib build.");
    exec.commands("mvn", "compile", "com.google.cloud.tools:jib-maven-plugin:1.6.0:build", "-Djib.to.image=" + image);
	}

	@Override
	public void push() {
	}
}
