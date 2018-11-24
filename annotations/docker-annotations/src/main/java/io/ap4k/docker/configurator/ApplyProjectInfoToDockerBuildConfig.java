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
package io.ap4k.docker.configurator;

import io.ap4k.decorator.Decorator;
import io.ap4k.docker.config.DockerBuildConfigFluent;
import io.ap4k.project.Project;
import io.ap4k.utils.Strings;

public class ApplyProjectInfoToDockerBuildConfig extends Decorator<DockerBuildConfigFluent> {

  private static final String APP_GROUP = "app.group";
  private static final String APP_NAME = "app.name";
  private static final String APP_VERSION = "app.version";

  private static final String DEFAULT_GROUP = "default";

  private final Project project;

  public ApplyProjectInfoToDockerBuildConfig(Project project) {
    this.project = project;
  }

  @Override
  public void visit(DockerBuildConfigFluent fluent) {
    fluent.withProject(project);
    fluent.withGroup(System.getProperty(APP_GROUP, Strings.isNotNullOrEmpty(fluent.getName()) ? fluent.getName() : DEFAULT_GROUP))
      .withName(System.getProperty(APP_NAME, Strings.isNotNullOrEmpty(fluent.getName()) ? fluent.getName() : project.getBuildInfo().getName()))
      .withVersion(System.getProperty(APP_VERSION, Strings.isNotNullOrEmpty(fluent.getVersion()) ? fluent.getVersion() : project.getBuildInfo().getVersion()));
  }
}
