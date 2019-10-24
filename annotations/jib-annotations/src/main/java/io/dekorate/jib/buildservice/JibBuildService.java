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
import io.dekorate.jib.config.JibBuildConfig;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.project.MavenInfoReader;
import io.dekorate.project.Project;
import io.dekorate.utils.Exec;
import io.dekorate.utils.Images;
import io.dekorate.utils.Strings;

public class JibBuildService implements BuildService {

  private final Logger LOGGER = LoggerFactory.getLogger();

  private final String JIB_VERSION = "1.6.1";
  private final String BUILD = "build";
  private final String DOCKER_BUILD = "dockerBuild";
  private final String MAVEN_GOAL = "com.google.cloud.tools:jib-maven-plugin:%s:%s";

  private final Project project;
  private final JibBuildConfig config;
  private final String image;
  private final Exec.ProjectExec exec;

	public JibBuildService(Project project, ImageConfiguration config) {
    if (!(config instanceof JibBuildConfig)) {
      throw new IllegalArgumentException("JibBuildService expects an instance of JibBuildConfig.");
    }
		this.project = project;
    this.exec = Exec.inProject(project);
    this.image = Images.getImage(Strings.isNullOrEmpty(config.getRegistry()) ? "docker.io" : config.getRegistry() , config.getGroup(), config.getName(), config.getVersion());
		this.config = (JibBuildConfig) config;
	}


	@Override
	public void build() {
    LOGGER.info("Performing jib build.");
    if (project.getBuildInfo().getBuildTool().equals(MavenInfoReader.MAVEN)) {
      exec.commands("mvn", "compile", String.format(MAVEN_GOAL, JIB_VERSION, config.isDockerBuild() ? DOCKER_BUILD : BUILD), "-Djib.to.image=" + image);
    }
	}

	@Override
	public void push() {
	}
}
