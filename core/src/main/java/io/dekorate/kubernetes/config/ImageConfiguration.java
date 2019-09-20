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

package io.dekorate.kubernetes.config;

import java.util.Map;

import io.dekorate.project.Project;
import io.sundr.builder.annotations.Buildable;

@Buildable(builderPackage = "io.dekorate.deps.kubernetes.api.builder")
public class ImageConfiguration extends ApplicationConfiguration {

  private String registry;
  private String dockerFile;

  public ImageConfiguration() {
  }

  public ImageConfiguration(Project project, Map<ConfigKey, Object> attributes, String group, String name, String version, String registry, String dockerFile) {
    super(project, attributes, group, name, version);
    this.registry = registry;
    this.dockerFile = dockerFile;
  }

  public String getRegistry() {
    return registry;
  }

  public void setRegistry(String registry) {
    this.registry = registry;
  }

  public String getDockerFile() {
    return dockerFile;
  }

  public void setDockerFile(String dockerFile) {
    this.dockerFile = dockerFile;
  }
}
