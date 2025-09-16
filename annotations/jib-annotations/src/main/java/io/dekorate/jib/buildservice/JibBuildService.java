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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import io.dekorate.BuildService;
import io.dekorate.DekorateException;
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

  private final String JIB_VERSION = "3.4.5";
  private final String BUILD = "build";
  private final String DOCKER_BUILD = "dockerBuild";
  private final String MAVEN_GOAL = "com.google.cloud.tools:jib-maven-plugin:%s:%s";

  private final String JIB = "jib";
  private final String JIB_DOCKER_BUILD = "jibDockerBuild";
  private final String GRADLE_INIT = "--";

  private final Project project;
  private final ImageConfiguration config;
  private final String image;
  private final Exec.ProjectExec exec;

  public JibBuildService(Project project, ImageConfiguration config) {
    this.project = project;
    this.exec = Exec.inProject(project);
    this.image = Images.getImage(Strings.isNullOrEmpty(config.getRegistry()) ? "docker.io" : config.getRegistry(),
        config.getGroup(), config.getName(), config.getVersion());
    this.config = config;
  }

  @Override
  public void build() {
    LOGGER.info("Performing jib build.");
    if (project.getBuildInfo().getBuildTool().equals(MavenInfoReader.MAVEN)) {
      mavenBuild();
    }
  }

  @Override
  public void push() {
    if (isDockerBuildEnabled(config)) {
      exec.commands("docker", "push", image);
    }
  }

  private void mavenBuild() {
    exec.commands("mvn", "compile",
        String.format(MAVEN_GOAL, JIB_VERSION, isDockerBuildEnabled(config) ? DOCKER_BUILD : BUILD),
        "-Djib.to.image=" + image);
  }

  private void gradleBuild() {
    Path outputPath = null;
    String content = null;
    URL url = getClass().getClassLoader().getResource("init.gradle");
    try (InputStream is = url.openStream()) {
      content = Strings.read(is);
    } catch (IOException e) {
      throw DekorateException.launderThrowable("Error reading init.gradle from resources.", e);
    }

    try {
      outputPath = Files.createTempFile("dekorate", "init-gradle");
      Files.write(outputPath, content.getBytes("UTF-8"));
    } catch (IOException e) {
      throw DekorateException.launderThrowable("Error writing init.gradle to tmp.", e);
    }
    exec.commands("gradle", isDockerBuildEnabled(config) ? JIB_DOCKER_BUILD : JIB, "--init-script",
        outputPath.toAbsolutePath().toString());
  }

  private static boolean isDockerBuildEnabled(ImageConfiguration config) {
    if (!(config instanceof JibBuildConfig)) {
      //DockerBuild is the default behavior, so if generic config is passed, then do use docker build.
      return true;
    }
    JibBuildConfig jibConfig = (JibBuildConfig) config;
    return jibConfig.isDockerBuild();
  }
}
