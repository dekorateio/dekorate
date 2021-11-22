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

import java.util.Collection;

import io.dekorate.BuildService;
import io.dekorate.BuildServiceApplicablility;
import io.dekorate.BuildServiceFactory;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.docker.config.DockerBuildConfig;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.project.Project;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.HasMetadata;

public class DockerBuildServiceFactory implements BuildServiceFactory {

  private static final String DOCKER = "docker";
  private static final String MESSAGE_OK = "Docker build service is applicable.";
  private static final String MESSAGE_NOK = "Docker build service is not applicable to the project, due to not being able find Dockerfile at: %s. Please configure the correct path to the Dockerfile.";

  @Override
  public BuildService create(Project project, ImageConfiguration config) {
    return new DockerBuildService(project, config);
  }

  @Override
  public BuildService create(Project project, ImageConfiguration config, Collection<HasMetadata> resources) {
    return new DockerBuildService(project, config);
  }

  @Override
  public int order() {
    return 10;
  }

  @Override
  public String name() {
    return DOCKER;
  }

  @Override
  public BuildServiceApplicablility checkApplicablility(Project project, ImageConfiguration config) {
    if (!(config instanceof DockerBuildConfig)) {
      return new BuildServiceApplicablility(false, "Docker build config not found");
    }

    DockerBuildConfig dockerBuildConfig = (DockerBuildConfig) config;
    String dockerFile = Strings.isNotNullOrEmpty(dockerBuildConfig.getDockerFile()) ? dockerBuildConfig.getDockerFile()
        : "Dockerfile";
    boolean applicable = dockerBuildConfig.isEnabled() && project.getRoot().resolve(dockerFile).toFile().exists();
    String message = applicable
        ? MESSAGE_OK
        : String.format(MESSAGE_NOK, project.getRoot().resolve(dockerFile));
    return new BuildServiceApplicablility(applicable, message);
  }

  @Override
  public BuildServiceApplicablility checkApplicablility(Project project, ConfigurationSupplier<ImageConfiguration> supplier) {
    if (supplier.isExplicit()) {
      return new BuildServiceApplicablility(true, MESSAGE_OK);
    }

    return checkApplicablility(project, supplier.get());
  }
}
