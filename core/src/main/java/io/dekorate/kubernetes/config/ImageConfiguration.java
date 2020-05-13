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

import io.dekorate.deps.jackson.annotation.JsonTypeInfo;
import io.dekorate.project.Project;
import io.dekorate.utils.Strings;
import io.sundr.builder.annotations.Buildable;

@Buildable(builderPackage = "io.dekorate.deps.kubernetes.api.builder")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class ImageConfiguration extends ApplicationConfiguration {

  private String registry;
  private String group;
  private String name;
  private String version;

  private String image;
  private String dockerFile;
  private boolean autoBuildEnabled;
  private boolean autoPushEnabled;

  public static ImageConfiguration from(ApplicationConfiguration applicationConfiguration) {
    return new ImageConfigurationBuilder()
      .withProject(applicationConfiguration.getProject())
      .withName(applicationConfiguration.getName())
      .withVersion(applicationConfiguration.getVersion())
      .withAttributes(applicationConfiguration.getAttributes())
      .build();
  }

  public ImageConfiguration() {
  }

  public ImageConfiguration(Project project, Map<ConfigKey, Object> attributes, String registry, String group, String name,
                            String version, String image, String dockerFile, boolean autoBuildEnabled, boolean autoPushEnabled) {
    super(project, attributes, group, name, version);
    this.registry = registry;
    this.group = Strings.isNotNullOrEmpty(group) ? group : System.getProperty("user.name");
    this.name = name;
    this.version = version;
    this.image = image;
    this.dockerFile = dockerFile;
    this.autoBuildEnabled = autoBuildEnabled;
    this.autoPushEnabled = autoPushEnabled;
  }

  public String getRegistry() {
    return registry;
  }

  public void setRegistry(String registry) {
    this.registry = registry;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public String getDockerFile() {
    return dockerFile;
  }

  public void setDockerFile(String dockerFile) {
    this.dockerFile = dockerFile;
  }

  public boolean isAutoBuildEnabled() {
    return autoBuildEnabled;
  }

  public void setAutoBuildEnabled(boolean autoBuildEnabled) {
    this.autoBuildEnabled = autoBuildEnabled;
  }

  public boolean isAutoPushEnabled() {
    return autoPushEnabled;
  }

  public void setAutoPushEnabled(boolean autoPushEnabled) {
    this.autoPushEnabled = autoPushEnabled;
  }

}
